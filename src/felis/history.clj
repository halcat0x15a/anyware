(ns felis.history
  (:require [felis.buffer :as buffer]))

(defrecord History [present past futures])

(def default (History. buffer/default nil []))

(def path [:root :workspace :history])

(defn undo [history]
  (if-let [past (:past history)]
    (assoc past
      :futures (conj (:futures past) history))
    history))

(defn commit [history buffer]
  (History. buffer history []))
