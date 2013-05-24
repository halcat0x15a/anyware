(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.parser :as parser]
            [anyware.core.command :as command]))

(defrecord Editor [frame command keymap clipboard])

(def buffer (atom "*scratch*"))

(def keymap (atom command/default))

(def history
  (vary-meta (history/create buffer/empty) assoc :parser parser/id))

(def frame (frame/create @buffer history))

(def default (Editor. frame history @keymap (history/create "")))
