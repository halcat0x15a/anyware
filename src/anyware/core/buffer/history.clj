(ns anyware.core.buffer.history
  (:require [clojure.zip :as zip]
            [anyware.core.buffer :as buffer]))

(def create (comp zip/down zip/vector-zip vector))

(def default (create buffer/default))

(def undo (comp zip/right zip/up))

(def redo (comp zip/down zip/left))

(defn commit [buffer history]
  (-> history (zip/insert-left [buffer]) redo))
