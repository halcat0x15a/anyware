(ns anyware.core
  (:refer-clojure :exclude [format])
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.path :as path]
            [anyware.core.editor :as editor]
            [anyware.core.keymap :as keymap]
            [anyware.core.format :as format]
            [anyware.core.api :as api])
  (:import clojure.lang.ExceptionInfo))

(def editor (atom editor/default :validator path/validate))

(defprotocol Anyware
  (keycode [this event])
  (render [this editor]))

(defn run
  ([{:keys [mode] :as editor} anyware event]
     (try
       (api/run editor (keycode anyware event))
       (catch ExceptionInfo e
         (assoc-in editor path/minibuffer (buffer/read (str e))))))
  ([anyware event]
     (render anyware (swap! editor run anyware event))))
