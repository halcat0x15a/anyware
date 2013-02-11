(ns felis.test.lisp
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [clojure.test :refer [deftest testing with-test are]]
            [felis.lisp :as lisp]))

(defn literal []
  (gen/rand-nth
   [nil
    (gen/long)
    (gen/double)
    (gen/boolean)
    (gen/char)
    (gen/string)
    (gen/keyword)]))

(defspec if-then-else
  (fn [predicate consequent alternative]
    (lisp/eval `(if ~predicate ~consequent ~alternative)))
  [^boolean predicate
   ^{:tag `literal} consequent
   ^{:tag `literal} alternative]
  (is (if predicate
        (= % consequent)
        (= % alternative))))

(defspec do-sequence
  (fn [sequence]
    (lisp/eval (cons 'do sequence)))
  [^{:tag (list `literal)} sequence]
  (is (= % (last sequence))))

(defspec definiton-and-get
  (fn [symbol value]
    (lisp/eval (list 'do (list 'def symbol value) symbol)))
  [^symbol symbol ^{:tag `literal} value]
  (is (= % value)))

(defspec constant-lambda
  (fn [value]
    (lisp/eval (list (list 'fn [] value))))
  [^{:tag `literal} value]
  (is (= % value)))

(defspec conjunction
  (fn [predicates]
    (lisp/eval (cons 'and predicates)))
  [^{:tag (list boolean)} predicates]
  (is (= % (reduce #(and % %2) true predicates))))

(defspec disjunction
  (fn [predicates]
    (lisp/eval (cons 'or predicates)))
  [^{:tag (list boolean)} predicates]
  (is (= % (reduce #(or % %2) false predicates))))

(with-test
  (defn factorial [n]
    (cond (zero? n) 1
          (= n 1) 1
          :else (* n (factorial (dec n)))))
  (are [x] (= x 24)
       (factorial 4)
       (lisp/eval
        '(do
           (defn factorial [n]
             (cond (zero? n) 1
                   (= n 1) 1
                   :else (* n (factorial (dec n)))))
           (factorial 4)))))

(with-test
  (defn fib [n]
    (cond (zero? n) 0
          (= n 1) 1
          :else (+ (fib (- n 2)) (fib (dec n)))))
  (are [x] (= x 13)
       (fib 7)
       (lisp/eval
        '(do
           (defn fib [n]
             (cond (zero? n) 0
                   (= n 1) 1
                   :else (+ (fib (- n 2)) (fib (dec n)))))
           (fib 7)))))
