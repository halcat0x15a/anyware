(ns anyware.core.lisp
  (:refer-clojure :exclude [read-string])
  (:require [anyware.core.lisp.lexer :as lexer]
            [anyware.core.lisp.evaluator :as evaluator]
            [anyware.core.lisp.environment :as environment]))

(defn read-string
  ([string] (read-string environment/global string))
  ([env string]
     (->> string
          lexer/lisp
          :result
          (cons 'do)
          (evaluator/eval env))))
