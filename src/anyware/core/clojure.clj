(ns anyware.core.clojure
  (:refer-clojure :exclude [keyword comment])
  (:require [anyware.core.parser :refer :all]))

(def definition
  (chain (fn [def space name] [(->Label :special def) space (->Label :symbol name)])
         (parser #"^def\w*")
         (parser #"^\s+")
         (parser #"^\w+")))

(def special
  (chain (fn [special _] (->Label :special special))
         (parser #"^(def\w*|if|do|let|quote|var|fn|loop|recur|throw|try)")
         (fail #"^\w")))

(defn string [input]
  (fmap (parse #"^\"[\s\S]*?\"" input) (partial ->Label :string)))

(defn keyword [input]
  (fmap (parse #"^:[^\(\)\[\]\{\}\s]+" input) (partial ->Label :keyword)))

(defn comment [input]
  (fmap (parse #"^;.*" input) (partial ->Label :comment)))

(def expression
  (many (choice definition
                special
                string
                keyword
                comment
                (parser #"^[\s\S]"))))
