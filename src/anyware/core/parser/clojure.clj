(ns anyware.core.parser.clojure
  (:refer-clojure :exclude [symbol keyword comment list vector map])
  (:require [anyware.core.parser :as parser]
            [anyware.core.parser.ast :as ast]
            [anyware.core.parser.language :as language]))

(declare expressions)

(def identifier (parser/regex #"^[^:;\(\)\[\]\{\}\"\s]+"))

(def space (parser/regex #"^\s+"))

(def definition
  (->> #"^def\w*" parser/regex (ast/map :special)))

(def symbol (ast/map :symbol identifier))

(defn definition-form [input]
  ((parser/and definition
               space
               symbol
               space
               expressions)
   input))

(def special
  (->> #"^(if|do|let|quote|var|fn|loop|recur|throw|try)"
       parser/regex
       (ast/map :special)))

(defn special-form [input]
  ((parser/and special (parser/maybe expressions)) input))

(def number
  (->> #"^\d+\.\d*|^\d+" parser/regex (ast/map :number)))

(def string
  (->> #"^\"[\s\S]*?\"" parser/regex (ast/map :string)))

(def keyword
  (->> #"^:[^\(\)\[\]\{\}\s]+" parser/regex (ast/map :keyword)))

(def comment
  (->> #"^;.*" parser/regex  (ast/map :comment)))

(defn parenthesis [label left parser right]
  (->> (parser/and (parser/literal left) parser (parser/literal right))
       (ast/map label)))

(defn list [input]
  ((parenthesis :list
                "("
                (parser/or definition-form special-form expressions)
                ")")
   input))

(defn vector [input]
  ((parenthesis :vector "[" expressions "]") input))

(defn map [input]
  ((parenthesis :map "{" expressions "}") input))

(def expression
  (parser/and (parser/maybe space)
              (parser/or number
                         identifier
                         keyword
                         comment
                         string
                         list
                         vector
                         map)
              (parser/maybe space)))

(def expressions (parser/repeat expression))

(defmethod language/extension "clj" [_] expression)
