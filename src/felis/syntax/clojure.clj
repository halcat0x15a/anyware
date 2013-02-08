(ns felis.syntax.clojure
  (:refer-clojure :exclude [keyword comment])
  (:require [felis.syntax :as syntax]
            [felis.node :as node]))

(def identifier (syntax/parser #"^[^\":;]"))

(def definition
  (syntax/parser #"^\((def.*?)(\s+)(\S*)"
                 (fn [[_ definition space name]]
                   (str \(
                        (node/tag :span {:class :special} definition)
                        space
                        (node/tag :span {:class :name} name)))))

(def special
  (syntax/parser
   #"^\((if|do|let|quote|var|fn|loop|recur|throw|try)(\s+)"
   (fn [[_ special space]]
     (str \( (node/tag :span {:class :special} special) space))))

(def string
  (syntax/parser #"^\".*\""
                 (partial node/tag :span {:class :string})))

(def keyword
  (syntax/parser #"^:[^\(\)\s]+" (partial node/tag :span {:class :keyword})))

(def comment
  (syntax/parser #"^;.*" (partial node/tag :span {:class :comment})))

(def syntax
  (syntax/repeat (syntax/or comment string keyword definition special identifier)))
