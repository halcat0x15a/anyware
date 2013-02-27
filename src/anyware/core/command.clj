(ns anyware.core.command)

(defmulti exec (fn [[f & args] editor] f))
(defmethod exec :default [_ editor] editor)
