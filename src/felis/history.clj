(ns felis.history
  (:require [felis.buffer :as buffer]))

(defrecord History [present past futures])

(def default (History. buffer/default nil []))

(defn undo [history]
  (if-let [past (:past history)]
    (assoc past
      :futures (conj (:futures past) history))))

(defn commit [buffer history]
  (History. buffer history []))
