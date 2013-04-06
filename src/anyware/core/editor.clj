(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.record :as record]
            [anyware.core.command :as command]))

(defrecord Editor [frame minibuffer mode])

(defn exec [editor]
  ((->> editor
        (record/get record/minibuffer)
        buffer/command
        (apply command/exec))
   editor))

(def buffer (atom "*scratch*"))

(def mode (atom :normal))

(def history (history/create buffer/empty))

(def frame (frame/create @buffer history))

(def default (Editor. frame history @mode))
