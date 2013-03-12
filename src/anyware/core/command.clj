(ns anyware.core.command
  (:require [clojure.string :as string]
            [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.buffer :as buffer]))

(defmulti exec (fn [[f & args] editor] f))
(defmethod exec :default [_ editor] editor)

(defn run [editor]
  (if-let [editor' (exec (->> editor
                              (lens/get record/minibuffer)
                              buffer/command)
                         editor)]
    (assoc editor' :minibuffer buffer/empty)
    editor))
