(ns felis.syntax.clojure
  (:refer-clojure :exclude [keyword comment])
  (:require [felis.parser :as parser]
            [felis.node :as node]))

(def identifier (parser/parser #"^[^\":;]"))

(def definition
  (parser/parser #"^\((def.*?)(\s+)(\S*)"
                 (fn [[_ definition space name]]
                   (str \(
                        (node/tag :span {:class :special} definition)
                        space
                        (node/tag :span {:class :name} name)))))

(def special
  (parser/parser
   #"^\((if|do|let|quote|var|fn|loop|recur|throw|try)(\s+)"
   (fn [[_ special space]]
     (str \( (node/tag :span {:class :special} special) space))))

(def string
  (parser/parser #"^\".*\""
                 (partial node/tag :span {:class :string})))

(def keyword
  (parser/parser #"^:[^\(\)\s]+" (partial node/tag :span {:class :keyword})))

(def comment
  (parser/parser #"^;.*" (partial node/tag :span {:class :comment})))

(def syntax
  (parser/repeat (parser/or comment string keyword definition special identifier)))
