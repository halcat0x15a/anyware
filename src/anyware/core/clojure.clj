(ns anyware.core.clojure
  (:refer-clojure :exclude [keyword comment])
  (:require [anyware.core.parser :refer :all]))

(defrecord Label [name value])

(def definition
  (<*> (fn [def space name]
         [(Label. :special def) space (Label. :symbol name)])
       #"^def\w*"
       #"^\s+"
       #"^\w+"))

(def special
  (<*> (fn [special space] [(Label. :special special) space])
       #"^(if|do|let|quote|var|fn|loop|recur|throw|try)"
       #"^(\s+|$)"))

(def string (fmap #"^\"[\s\S]*?\"" (partial ->Label :string)))

(def keyword (fmap #"^:[^\(\)\[\]\{\}\s]+" (partial ->Label :keyword)))

(def comment (fmap #"^;.*" (partial ->Label :comment)))

(def expression
  (many
   (<|> definition
        special
        string
        keyword
        comment
        #"[\s\S]")))

(->> (parse expression (slurp "project.clj")) :value flatten (take 10))
