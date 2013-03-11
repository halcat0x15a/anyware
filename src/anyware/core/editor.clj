(ns anyware.core.editor
  (:require [anyware.core.buffer.history :as history]
            [anyware.core.buffer.list :as list]
            [anyware.core.lens :as lens]
            [anyware.core.mode :as mode]))

(defn run [key {:keys [mode] :as editor}]
  (if-let [f ((merge (mode/keymap mode)
                     {:escape (lens/set :mode :normal)})
              key)]
    (f editor)
    (mode/default mode key editor)))

(defrecord Editor [list minibuffer mode])

(def default (Editor. list/empty history/empty :normal))
