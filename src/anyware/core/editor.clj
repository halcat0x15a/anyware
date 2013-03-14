(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.lens :as lens]
            [anyware.core.mode :as mode]))

(defn run [key {:keys [mode] :as editor}]
  (if-let [f ((merge (mode/keymap mode)
                     {:escape (lens/set :mode :normal)})
              key)]
    (f editor)
    (mode/default mode key editor)))

(def default (atom "*scratch*"))

(defrecord Editor [frame minibuffer mode])

(def history (history/create buffer/empty))

(def frame (frame/create @default history))

(def default (Editor. frame history :normal))
