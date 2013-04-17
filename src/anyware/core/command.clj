(ns anyware.core.command
  (:require [clojure.zip :as zip]
            [anyware.core.record :refer (modify)]
            [anyware.core.frame :as frame]
            [anyware.core.editor :as editor]))

(defmulti exec (fn [f & args] f))
(defmethod exec :default [_])

(defmethod exec "next" [_] (modify :frame zip/right))
(defmethod exec "prev" [_] (modify :frame zip/left))
(defmethod exec "new" [_ name]
  (modify :frame (frame/assoc name editor/history)))
