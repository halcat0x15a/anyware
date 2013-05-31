(ns anyware.core.clojure
  (:require [anyware.core.parser :as parser]
            [anyware.core.tree :as tree]))

(def definitions
  (parser/product
   (tree/map :special #"^def\w*")
   #"^\s+"
   (tree/map :symbol #"^\w+")))

(def specials
  (parser/product
   \(
   (tree/map
    :special
    #"^(if|do|let|quote|var|fn|loop|recur|throw|try)")
   #"^\s+"))

(def strings (tree/map :string #"^\"[\s\S]*?\""))

(def keywords (tree/map :keyword #"^:[^\(\)\[\]\{\}\s]+"))

(def comments (tree/map :comment #"^;.*"))

(def expressions
  (parser/many
   (parser/sum
    definitions
    specials
    strings
    keywords
    comments
    #"[\s\S]")))
