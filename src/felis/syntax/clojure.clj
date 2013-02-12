(ns felis.syntax.clojure
  (:refer-clojure :exclude [keyword comment])
  (:require [felis.parser :as parser]
            [felis.html :as html]))

(def identifier (parser/parser #"^[^\":;]"))

(def definition
  (parser/parser #"^\((def.*?)(\s+)(\S*)"
                 (fn [[_ definition space name]]
                   ["("
                    (html/< :span {:class :special} definition)
                    space
                    (html/< :span {:class :name} name)])))

(def special
  (parser/parser
   #"^\((if|do|let|quote|var|fn|loop|recur|throw|try)(\s+)"
   (fn [[_ special space]]
     ["(" (html/< :span {:class :special} special) space])))

(def string
  (parser/parser #"^\".*\""
                 (partial html/< :span {:class :string})))

(def keyword
  (parser/parser #"^:[^\(\)\s]+"
                 (partial html/< :span {:class :keyword})))

(def comment
  (parser/parser #"^;.*"
                 (partial html/< :span {:class :comment})))

(def syntax
  (parser/repeat
   (parser/or comment string keyword definition special identifier)))
