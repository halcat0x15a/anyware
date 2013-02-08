(ns felis.syntax.scheme
  (:require [felis.syntax :as syntax]
            [felis.node :as node]))

(def identifier (syntax/parser #"^[^\"]"))

(def definition
  (syntax/parser #"\(define(\s+)(\S+)"
                 (fn [[_ space name]]
                   (str \(
                        (node/tag :span {:class :special} "define")
                        space
                        (node/tag :span {:class :name} name)))))

(def string
  (syntax/parser #"^\".*\""
                 (partial node/tag :span {:class :string})))

(def special
  (syntax/parser
   #"^\((quote|lambda|if|set!|begin|cond|and|or|case|let|let*|letrec|do|delay|else)(\s+)"
   (fn [[_ special space]]
     (str (node/tag :span {:class :special} special)
          space))))

(def syntax
  (syntax/repeat (syntax/or string definition special identifier)))
