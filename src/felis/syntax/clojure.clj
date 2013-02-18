(ns felis.syntax.clojure
  (:refer-clojure :exclude [keyword comment])
  (:require [felis.parser :as parser]
            [felis.html :as html]))

(def identifier (parser/regex #"^[^\":;]"))

(def definition
  (-> #"^\((def.*?)(\s+)(\S*)"
      parser/regex
      (parser/map (fn [[_ definition space name]]
                    ["("
                     (html/< :span {:class :special} definition)
                     space
                     (html/< :span {:class :name} name)]))))

(def special
  (-> #"^\((if|do|let|quote|var|fn|loop|recur|throw|try)(\s+)"
      parser/regex
      (parser/map (fn [[_ special space]]
                    ["(" (html/< :span {:class :special} special) space]))))

(def string
  (-> #"^\".*\""
      parser/regex 
      (parser/map (partial html/< :span {:class :string}))))

(def keyword
  (-> #"^:[^\(\)\s]+"
      parser/regex
      (parser/map (partial html/< :span {:class :keyword}))))

(def comment
  (-> #"^;.*"
      parser/regex
      (parser/map (partial html/< :span {:class :comment}))))

(def syntax
  (parser/repeat
   (parser/or comment string keyword definition special identifier)))
