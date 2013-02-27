(ns anyware.core
  (:require [anyware.core.html :as html]
            [anyware.core.editor :as editor]))

(def editor (atom editor/default))

(defprotocol Anyware
  (keycode [this event])
  (render [this html]))

(defn run
  ([anyware event {:keys [mode] :as editor}]
     ((mode (keycode anyware event)) editor))
  ([anyware event]
     (doto (swap! editor (partial run anyware event))
       (->> html/html html/write (render anyware)))))
