(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]
            [anyware.core.buffer.list :as list]
            [anyware.core.mode :as mode]))

(defrecord Editor [list minibuffer mode])

(def default
  (Editor. (->> buffer/empty history/create (list/create "*scratch*"))
           buffer/empty mode/normal))
