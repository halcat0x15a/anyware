(ns anyware.lisp
  (:refer-clojure :exclude [read-string])
  (:require [anyware.lisp.lexer :as lexer]
            [anyware.lisp.parser :as parser]
            [anyware.lisp.environment :as environment]))

(defn read-string
  ([string] (read-string (atom environment/global) string))
  ([env string]
     (->> string
          lexer/lisp
          :result
          (cons 'do)
          (parser/eval env))))
