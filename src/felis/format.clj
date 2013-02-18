(ns felis.format
  (:require [felis.parser :as parser]))

(def indent (parser/parser #"(.*)\n(.*)"
                           (fn [[_ left right]]
                             (str left \newline "  " right))))

(def lisp (parser/repeat indent))

(->> "(hello\nworld(hello\nworad)\n\n)"
     (parser/parse lisp)
     :destination
     persistent!
     (apply str))
