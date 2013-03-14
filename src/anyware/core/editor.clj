(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.api.keymap :as keymap]))

(def default (atom "*scratch*"))

(defn run [key {:keys [mode] :as editor}]
  (((keymap/keymap mode) key) editor))

(defrecord Editor [frame minibuffer mode])

(def history (history/create buffer/empty))

(def frame (frame/create @default history))

(def default (Editor. frame history :normal))
