(ns anyware.core.history
  (:require [clojure.zip :as zip]))

(defrecord Change [future current])

(def current [0 :current])

(def change (partial ->Change []))

(defn- make-node [change children] (assoc change :future children))

(def create
  (comp (partial zip/zipper :future :future make-node) change))

(defn undo [history]
  (if-let [history (zip/up history)]
    history
    (throw (ex-info "Already at oldest change" {}))))

(defn redo [history]
  (if-let [history (zip/down history)]
    history
    (throw (ex-info "Already at newest change" {}))))

(defn commit
  ([history] (commit (get-in history current) history))
  ([value history]
     (-> history (zip/insert-child (change value)) zip/down)))
(create 1)
