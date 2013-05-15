(ns anyware.core
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.editor :as editor]
            [anyware.core.keymap :as keymap]
            [anyware.core.emacs :as emacs]
            [anyware.core.vi :as vi]
            [anyware.core.api :as api])
  (:import clojure.lang.ExceptionInfo))

(def editor (atom editor/default :validator api/validate))

(defprotocol Anyware
  (keycode [this event])
  (render [this editor]))

(defn init []
  (doto api/commands
    (swap! assoc "emacs" #(assoc-in % api/mode emacs/keymap))
    (swap! assoc "vi" #(assoc-in % api/mode vi/normal))))

(defn run
  ([editor anyware event]
     (try
       (api/run editor (keycode anyware event))
       (catch ExceptionInfo e
         (assoc-in editor api/minibuffer (buffer/read (str e))))))
  ([anyware event]
     (render anyware (swap! editor run anyware event))))
