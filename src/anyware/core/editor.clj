(ns anyware.core.editor
  (:require [anyware.core.api :as api]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.parser :as parser]
            [anyware.core.command :as command]))

(defrecord Editor [frame command keymap clipboard])

(def buffer (atom "*scratch*"))

(def keymap (atom command/default))

(def window (history/create (api/read "")))

(def frame (frame/create @buffer window))

(def default (Editor. frame window @keymap (history/create "")))
