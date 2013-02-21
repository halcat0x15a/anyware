(ns felis.lisp.lexer
  (:require [clojure.string :as string]
            [felis.parser :as parser]))

(declare expression)

(def numbers
  (->> #"^\d+"
       parser/regex
       (parser/map (comp (partial reduce (fn [m n] (+ (* m 10) n)))
                         (partial map #(- (int %) (int \0)))))))

(def strings (parser/regex #"^\".*\""))

(def keywords
  (->> #"^:([^\(\)\[\]\"\s]*)"
       parser/regex
       (parser/map (fn [[_ string]]
                     (keyword string)))))

(def identifiers
  (->> #"^[^\(\)\[\]\":\s]+"
       parser/regex
       (parser/map symbol)))

(def space (parser/regex #"^\s*"))

(def lists
  (fn [input]
    ((->> (parser/and (parser/literal "(")
                      (parser/repeat expression)
                      (parser/literal ")"))
          (parser/map (fn [[_ lists _]] (apply list lists))))
     input)))

(def vectors
  (fn [input]
    ((->> (parser/and (parser/literal "[")
                      (parser/repeat expression)
                      (parser/literal "]"))
          (parser/map (fn [[_ vector _]] vector)))
     input)))

(def expression
  (fn [input]
    ((->> (parser/and
           space
           (parser/or strings keywords numbers lists vectors identifiers)
           space)
          (parser/map (fn [[_ expr _]] expr)))
     input)))

(def lisp
  (fn [input]
    ((parser/repeat expression) input)))
