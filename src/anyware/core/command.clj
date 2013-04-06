(ns anyware.core.command
  (:require [anyware.core.record :refer (modify)]
            [anyware.core.frame :as frame]))

(defmulti exec (fn [f & args] f))
(defmethod exec :default [_])

(defmethod exec "next" [_] (modify :frame frame/next))
