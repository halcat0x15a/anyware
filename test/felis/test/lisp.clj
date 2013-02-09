(ns felis.test.lisp
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
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

(defspec if-true-false
  (fn [predicate consequent alternative]
    (eval (list 'if predicate consequent alternative)))
  [^boolean predicate
   ^{:tag `literal} consequent
   ^{:tag `literal} alternative]
  (is (if predicate
        (= % consequent)
        (= % alternative))))

(defspec do-sequence
  (fn [sequence]
    (eval (cons 'do sequence)))
  [^{:tag (list `literal)} sequence]
  (is (= % (last sequence))))

(defspec definiton-and-get
  (fn [symbol value]
    (eval (list 'do (list 'def symbol value) symbol)))
  [^symbol symbol ^{:tag `literal} value]
  (is (= % value)))

(defspec constant-lambda
  (fn [value]
    (eval (list (list 'fn [] value))))
  [^{:tag `literal} value]
  (is (= % value)))
