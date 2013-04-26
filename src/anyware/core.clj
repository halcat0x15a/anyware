(ns anyware.core
  (:refer-clojure :exclude [format])
  (:require [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]
            [anyware.core.record :as record]
            [anyware.core.api.keymap :as keymap]
            [anyware.core.api.format :as format])
  (:import anyware.core.editor.Editor
           clojure.lang.ExceptionInfo))

(def validator (partial instance? Editor))

(def editor (atom editor/default :validator validator))

(defprotocol Anyware
  (keycode [this event])
  (render [this editor]))

(defn run
  ([{:keys [mode] :as editor} anyware event]
     (try
       (((mode @keymap/keymap) (keycode anyware event)) editor)
       (catch ExceptionInfo e
         (record/set record/minibuffer (buffer/read (str e))))))
  ([anyware event]
     (render anyware (swap! editor run anyware event))))
