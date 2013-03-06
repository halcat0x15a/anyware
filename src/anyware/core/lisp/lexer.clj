(ns anyware.core.lisp.lexer
  (:require [clojure.string :as string]
            [anyware.core.parser :as parser]))

(declare expression)

(defn- char->number [char]
  (- (int char) (int \0)))

(defn- string->number [string]
  (reduce (fn [m n] (+ (* m 10) n)) 0 (map char->number string)))

(def numbers (->> #"^\d+" parser/regex (parser/map string->number)))

(def strings (parser/regex #"^\".*\""))

(def keywords
  (->> #"^:([^\(\)\[\]\"\s]*)"
       parser/regex
       (parser/map (comp keyword second))))

(def identifiers
  (->> #"^[^\(\)\[\]\":\s]+" parser/regex (parser/map symbol)))

(def space (parser/regex #"^\s*"))

(defn- collection [left right f]
  (fn [input]
    ((->> (parser/and (parser/literal left)
                      (parser/repeat expression)
                      (parser/literal right))
          (parser/map f))
     input)))

(def lists (collection "(" ")" (comp (partial apply list) second)))

(def vectors (collection "[" "]" second))

(def expression
  (fn [input]
    ((->> (parser/and space
                      (parser/or strings
                                 keywords
                                 numbers
                                 lists
                                 vectors
                                 identifiers)
                      space)
          (parser/map second))
     input)))

(def lisp (fn [input] ((parser/repeat expression) input)))
