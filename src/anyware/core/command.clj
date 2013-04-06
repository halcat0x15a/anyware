(ns anyware.core.command
  (:require [anyware.core.lens :refer (modify)]
            [anyware.core.frame :as frame]))

(defmulti exec (fn [f & args] f))
(defmethod exec :default [_])

(defmethod exec "next" [_] (modify :frame frame/next))
