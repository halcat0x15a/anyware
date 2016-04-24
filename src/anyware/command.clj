(ns anyware.command
  (:require [anyware.editor :as editor]))

(defmacro open [path]
  `(fn [editor#]
     (editor/open ~(str path) editor#)))
