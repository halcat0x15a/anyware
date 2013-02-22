(ns felis.history
  (:require [felis.buffer :as buffer]))

(defrecord History [present past futures])

(def default (History. buffer/default nil []))

(defn undo [{:keys [past futures] :as history}]
  (if-not (nil? past)
    (assoc past
      :futures (conj futures history))))

(defn redo [{:keys [past futures] :as history}]
  (if-not (empty? futures)
    (peek futures)))

(defn commit [buffer history]
  (History. buffer history []))
