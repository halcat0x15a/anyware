(ns felis.lisp
  (:refer-clojure :exclude [read-string])
  (:require [felis.lisp.lexer :as lexer]
            [felis.lisp.parser :as parser]
            [felis.lisp.environment :as environment]))

(defn read-string
  ([string] (read-string (atom environment/global) string))
  ([env string]
     (->> string
          lexer/lisp
          :result
          (cons 'do)
          (parser/eval env))))
