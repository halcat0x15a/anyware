(ns anyware.test.lisp
  (:require [clojure.test.generative :refer (defspec)]
            [clojure.data.generators :as gen]
            [clojure.test :refer (deftest testing with-test is are)]
            [anyware.core.lisp :as lisp]
            [anyware.core.lisp.parser :as parser]
            [anyware.core.lisp.environment :as environment]))

(defn literal []
  (gen/rand-nth
   [nil
    (gen/long)
    (gen/double)
    (gen/boolean)
    (gen/string)
    (gen/keyword)]))

(defspec if-then-else
  (fn [predicate consequent alternative]
    (parser/eval `(if ~predicate ~consequent ~alternative)))
  [^boolean predicate
   ^{:tag `literal} consequent
   ^{:tag `literal} alternative]
  (is (if predicate
        (= % consequent)
        (= % alternative))))

(defspec do-sequence
  (fn [sequence]
    (parser/eval (cons 'do sequence)))
  [^{:tag (list `literal)} sequence]
  (is (= % (last sequence))))

(defspec definiton-and-get
  (fn [symbol value]
    (parser/eval (list 'do (list 'def symbol value) symbol)))
  [^symbol symbol ^{:tag `literal} value]
  (is (= % value)))

(defspec constant-lambda
  (fn [value]
    (parser/eval (list (list 'fn [] value))))
  [^{:tag `literal} value]
  (is (= % value)))

(defspec conjunction
  (fn [predicates]
    (parser/eval (cons 'and predicates)))
  [^{:tag (list boolean)} predicates]
  (is (= % (reduce #(and % %2) true predicates))))

(defspec disjunction
  (fn [predicates]
    (parser/eval (cons 'or predicates)))
  [^{:tag (list boolean)} predicates]
  (is (= % (reduce #(or % %2) false predicates))))

(with-test
  (defn factorial [n]
    (cond (zero? n) 1
          (= n 1) 1
          :else (* n (factorial (dec n)))))
  (are [x] (= x 24)
       (factorial 4)
       (parser/eval
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
       (parser/eval
        '(do
           (defn fib [n]
             (cond (zero? n) 0
                   (= n 1) 1
                   :else (+ (fib (- n 2)) (fib (dec n)))))
           (fib 7)))))

(def env (atom environment/global))

(deftest register-machine
  (testing "load file"
    (is (lisp/read-string env (slurp "resources/scheme.clj")))
    (is (lisp/read-string env (slurp "resources/register_machine.clj")))))
