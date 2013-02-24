(ns anyware.history
  (:require [clojure.zip :as zip]
            [anyware.buffer :as buffer]))

(defn create [buffer]
  (-> buffer
      vector
      zip/vector-zip
      zip/down))

(def default (create buffer/default))

(defn undo [history]
  (-> history
      zip/up
      zip/left))

(defn redo [history]
  (-> history
      zip/right
      zip/down))

(defn commit [buffer history]
  (-> history
      (zip/insert-right [buffer])
      redo))
