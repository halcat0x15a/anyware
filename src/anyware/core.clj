(ns anyware.core
  (:refer-clojure :exclude [format])
  (:require [anyware.core.editor :as editor]
            [anyware.core.format :as format]
            [anyware.core.keymap :as keymap]))

(def editor (atom editor/default))

(defprotocol Anyware
  (keycode [this event])
  (format [this])
  (render [this string]))

(defn run
  ([anyware event {:keys [mode] :as editor}]
     (((mode @keymap/keymap) (keycode anyware event)) editor))
  ([anyware event]
     (render anyware
             (format/render (format anyware)
                            (swap! editor (partial run anyware event))))))
