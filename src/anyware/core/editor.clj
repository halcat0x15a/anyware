(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.list :as list]
            [anyware.core.lens :as lens]
            [anyware.core.mode :as mode]))

(defn run [key {:keys [mode] :as editor}]
  (if-let [f ((merge (mode/keymap mode)
                     {:escape (lens/set :mode :normal)})
              key)]
    (f editor)
    (mode/default mode key editor)))

(def default (atom "*scratch*"))

(defrecord Editor [list minibuffer mode])

(def default-history (history/create buffer/empty))

(def default-list (list/create @default default-history))

(def default (Editor. default-list default-history :normal))
