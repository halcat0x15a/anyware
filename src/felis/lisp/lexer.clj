(ns felis.lisp.lexer
  (:refer-clojure :exclude [int keyword list vector])
  (:require [clojure.string :as string]
            [felis.parser :as parser]))

(declare expression)

(def number
  (-> (parser/regex #"^\d+")
      (parser/map (comp (partial reduce (fn [m n] (+ (* m 10) n)))
                        (partial map #(- (int %) (int \0)))))))

(def string (parser/regex #"^\".*\""))

(def keywords
  (-> (parser/regex #"^:([^\(\)\[\]\"\s]*)")
      (parser/map (fn [[_ string]]
                    (keyword string)))))

(def identifier
  (parser/map (parser/regex #"^[^\(\)\[\]\":\s]+") symbol))

(def space (parser/regex #"^\s*"))

(def list
  (fn [input]
    ((-> (parser/and (parser/literal "(")
                     (parser/repeat expression)
                     (parser/literal ")"))
         (parser/map (fn [[_ list _]] (apply core/list list))))
     input)))

(def vector
  (fn [input]
    ((-> (parser/and (parser/literal "[")
                     (parser/repeat expression)
                     (parser/literal "]"))
         (parser/map (fn [[_ vector _]] vector)))
     input)))

(def expression
  (fn [input]
    ((-> (parser/and
          space
          (parser/or string keywords number list vector identifier)
          space)
         (parser/map (fn [[_ expr _]] expr)))
     input)))

(def lisp
  (fn [input]
    ((parser/repeat expression) input)))
