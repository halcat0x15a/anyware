(ns anyware.core.api.command
  (:require [clojure.zip :as zip]
            [anyware.core.path :refer [frame]]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]))

(defmulti exec (fn [editor f & args] f))
(defmethod exec :default [editor _] editor)

(defmethod exec "next" [editor _]
  (update-in editor frame frame/next))
(defmethod exec "prev" [editor _]
  (update-in editor frame frame/prev))
(defmethod exec "new" [editor _ name]
  (update-in editor frame (frame/update name (history/create ""))))
