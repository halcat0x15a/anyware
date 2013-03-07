(ns anyware.test.lisp
  (:require [clojure.test.generative :refer (defspec)]
            [clojure.data.generators :as gen]
            [clojure.test :refer (deftest testing with-test is are)]
            [anyware.core.lisp :as lisp]
            [anyware.core.lisp.evaluator :as evaluator]
            [anyware.core.lisp.environment :as environment]))

(defn literal []
  (gen/rand-nth
   [nil
    (gen/long)
    (gen/boolean)
    (gen/string)
    (gen/keyword)]))

(defspec if-then-else
  (fn [predicate consequent alternative]
    (evaluator/eval `(if ~predicate ~consequent ~alternative)))
  [^boolean predicate
   ^{:tag `literal} consequent
   ^{:tag `literal} alternative]
  (is (if predicate
        (= % consequent)
        (= % alternative))))

(defspec do-sequence
  (fn [sequence]
    (evaluator/eval (cons 'do sequence)))
  [^{:tag (list `literal)} sequence]
  (is (= % (last sequence))))

(defspec constant-lambda
  (fn [value]
    (evaluator/eval (list (list 'fn [] value))))
  [^{:tag `literal} value]
  (is (= % value)))

(defspec conjunction
  (fn [predicates]
    (evaluator/eval (cons 'and predicates)))
  [^{:tag (list boolean)} predicates]
  (is (= % (reduce #(and % %2) true predicates))))

(defspec disjunction
  (fn [predicates]
    (evaluator/eval (cons 'or predicates)))
  [^{:tag (list boolean)} predicates]
  (is (= % (reduce #(or % %2) false predicates))))

(with-test
  (defn factorial [n]
    (cond (zero? n) 1
          (= n 1) 1
          :else (* n (factorial (dec n)))))
  (are [x] (= x 24)
       (factorial 4)
       (evaluator/eval
        '(letfn [(factorial [n]
                   (if (< n 2)
                     1
                     (* n (factorial (dec n)))))]
           (factorial 4)))))

(with-test
  (defn fib [n]
    (cond (zero? n) 0
          (= n 1) 1
          :else (+ (fib (- n 2)) (fib (dec n)))))
  (are [x] (= x 13)
       (fib 7)
       (evaluator/eval
        '(letfn [(fib [n]
                   (if (< n 2)
                     n
                     (+ (fib (- n 2)) (fib (dec n)))))]
           (fib 7)))))
