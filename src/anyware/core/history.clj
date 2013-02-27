(ns anyware.core.history
  (:require [clojure.zip :as zip]
            [anyware.core.buffer :as buffer]))

(defn create [buffer]
  (-> buffer
      vector
      zip/vector-zip
      zip/down))

(def default (create buffer/default))

(defn undo [history]
  (-> history
      zip/up
      zip/right))

(defn redo [history]
  (-> history
      zip/left
      zip/down))

(defn commit [buffer history]
  (-> history
      (zip/insert-left [buffer])
      redo))
