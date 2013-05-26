(ns anyware.core.language.clojure
  (:refer-clojure :exclude [symbol keyword comment list vector map])
  (:require [anyware.core.parser :as parser]
            [anyware.core.tree :as tree]))

(declare expressions)

(def identifier (parser/regex #"^[^:;\(\)\[\]\{\}\"\s]+"))

(def space (parser/regex #"^\s+"))

(def definition
  (->> #"^def\w*" parser/regex (tree/map :special)))

(def symbol (tree/map :symbol identifier))

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
       (tree/map :special)))

(defn special-form [input]
  ((parser/and special (parser/maybe expressions)) input))

(def number
  (->> #"^\d+\.\d*|^\d+" parser/regex (tree/map :number)))

(def string
  (->> #"^\"[\s\S]*?\"" parser/regex (tree/map :string)))

(def keyword
  (->> #"^:[^\(\)\[\]\{\}\s]+" parser/regex (tree/map :keyword)))

(def comment
  (->> #"^;.*" parser/regex  (tree/map :comment)))

(defn parenthesis [label left parser right]
  (->> (parser/and (parser/literal left) parser (parser/literal right))
       (tree/map label)))

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
