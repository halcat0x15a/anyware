(ns anyware.core
  (:require [anyware.core.editor :as editor]))

(def editor (atom editor/default))

(defprotocol Anyware
  (keycode [this event])
  (render [this html]))

(defn run
  ([anyware event editor]
     (editor/run (keycode anyware event) editor))
  ([anyware event]
     (swap! editor (partial run anyware event))))
