(ns anyware.core
  (:refer-clojure :exclude [format])
  (:require [anyware.core.editor :as editor]
            [anyware.core.format :as format]
            [anyware.core.keymap :as keymap])
  (:import anyware.core.editor.Editor))

(def validator (partial instance? Editor))

(def editor (atom editor/default :validator validator))

(defprotocol Anyware
  (keycode [this event])
  (render [this editor]))

(defn run
  ([{:keys [mode] :as editor} anyware event]
     (((mode @keymap/keymap) (keycode anyware event)) editor))
  ([anyware event]
     (render anyware (swap! editor run anyware event))))
