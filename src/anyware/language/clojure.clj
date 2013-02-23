(ns anyware.language.clojure
  (:refer-clojure :exclude [symbol keyword comment list vector map])
  (:require [anyware.parser :as parser]
            [anyware.parser.html :as html]))

(declare expressions)

(def identifier (parser/regex #"^[^:;\(\)\[\]\{\}\"\s]+"))

(def space (parser/regex #"^\s+"))

(def definition
  (->> #"^def\w*" parser/regex (html/< "special")))

(def symbol (html/< "symbol" identifier))

(defn definition-form [input]
  ((-> (parser/and definition
                   space
                   symbol
                   space
                   expressions)
       html/seq)
   input))

(def special
  (->> #"^(if|do|let|quote|var|fn|loop|recur|throw|try)"
       parser/regex
       (html/< "special")))

(defn special-form [input]
  ((-> (parser/and special (parser/maybe expressions)) html/seq)
   input))

(def number
  (->> #"^\d+\.\d*|^\d+"
       parser/regex 
       (html/< "number")))

(def string
  (->> #"^\"[\s\S]*?\""
       parser/regex 
       (html/< "string")))

(def keyword
  (->> #"^:[^\(\)\[\]\{\}\s]+"
       parser/regex 
       (html/< "keyword")))

(def comment
  (->> #"^;.*"
       parser/regex 
       (html/< "comment")))

(defn parenthesis [class left parser right]
  (->> (parser/and (parser/literal left) parser (parser/literal right))
       html/seq
       (html/< class)))

(defn list [input]
  ((parenthesis "list"
                "("
                (parser/or definition-form
                           special-form
                           expressions)
                ")")
   input))

(defn vector [input]
  ((parenthesis "vector" "[" expressions "]") input))

(defn map [input]
  ((parenthesis "map" "{" expressions "}") input))

(def expression
  (-> (parser/and (parser/maybe space)
                  (parser/or number
                             identifier
                             keyword
                             comment
                             string
                             list
                             vector
                             map)
                  (parser/maybe space))
      html/seq))

(def expressions
  (-> expression
      parser/repeat
      html/seq))
