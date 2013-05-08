(ns anyware.core
  (:refer-clojure :exclude [format])
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.editor :as editor]
            [anyware.core.keymap :as keymap]
            [anyware.core.format :as format]
            [anyware.core.api :as api])
  (:import clojure.lang.ExceptionInfo))

(def editor (atom editor/default :validator api/validate))

(defprotocol Anyware
  (keycode [this event])
  (render [this editor]))

(defn run
  ([editor anyware event]
     (try
       (api/run editor (keycode anyware event))
       (catch ExceptionInfo e
         (assoc-in editor api/minibuffer (buffer/read (str e))))))
  ([anyware event]
     (render anyware (swap! editor run anyware event))))
