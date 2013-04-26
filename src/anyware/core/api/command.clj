(ns anyware.core.api.command
  (:require [clojure.zip :as zip]
            [anyware.core.frame :as frame]
            [anyware.core.editor :as editor]))

(defmulti exec (fn [editor f & args] f))
(defmethod exec :default [editor _] editor)

(defmethod exec "next" [editor _] (get-in editor [:frame] frame/next))
(defmethod exec "prev" [editor _] (get-in editor [:frame] frame/prev))
(defmethod exec "new" [editor _ name]
  (get-in editor [:frame] (frame/update name editor/history)))
