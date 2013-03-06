(ns anyware.core.lisp
  (:refer-clojure :exclude [read-string])
  (:require [anyware.core.lisp.lexer :as lexer]
            [anyware.core.lisp.parser :as parser]
            [anyware.core.lisp.environment :as environment]))

(defn read-string
  ([string] (read-string environment/global string))
  ([env string]
     (->> string
          lexer/lisp
          :result
          (cons 'do)
          (parser/eval env))))
