(ns anyware.core.command
  (:require [clojure.string :as string]
            [anyware.core.buffer :as buffer]))

(defmulti exec (fn [[f & args] editor] f))
(defmethod exec :default [_ editor] editor)

(defn run [editor]
  (if-let [editor' (exec (-> editor
                             :minibuffer
                             buffer/write
                             (string/split #" "))
                         editor)]
    (assoc editor' :minibuffer buffer/empty)
    editor))
