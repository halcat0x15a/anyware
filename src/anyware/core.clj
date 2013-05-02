(ns anyware.core
  (:refer-clojure :exclude [format])
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.path :as path]
            [anyware.core.api.editor :as editor]
            [anyware.core.api.keymap :as keymap]
            [anyware.core.api.format :as format])
  (:import anyware.core.api.editor.Editor
           clojure.lang.ExceptionInfo))

(def editor (atom editor/default))

(defprotocol Anyware
  (keycode [this event])
  (render [this editor]))

(defn run
  ([{:keys [mode] :as editor} anyware event]
     (try
       (mode (keycode anyware event) editor)
       (catch ExceptionInfo e
         (assoc-in editor path/minibuffer (buffer/read (str e))))))
  ([anyware event]
     (render anyware (swap! editor run anyware event))))
