(ns anyware.core
  (:refer-clojure :exclude [format])
  (:require [anyware.core.editor :as editor]
            [anyware.core.format :as format]))

(def editor (atom editor/default))

(defprotocol Anyware
  (keycode [this event])
  (format [this])
  (render [this string]))

(defn run
  ([anyware event editor]
     (editor/run (keycode anyware event) editor))
  ([anyware event]
     (render anyware
             (format/render (format anyware)
                            (swap! editor (partial run anyware event))))))
