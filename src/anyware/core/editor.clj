(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.parser :as parser]
            [anyware.core.keymap :as keymap]))

(defrecord Editor [frame command mode clipboard])

(def buffer (atom "*scratch*"))

(def mode (atom keymap/default))

(def history
  (history/create (with-meta buffer/empty {:parser parser/id})))

(def frame (frame/create @buffer history))

(def default (Editor. frame history @mode (history/create "")))
