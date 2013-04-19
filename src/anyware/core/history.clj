(ns anyware.core.history
  (:require [clojure.zip :as zip]))

(defrecord Change [list value])

(def change (partial ->Change []))

(def branch? (partial instance? Change))

(defn- make-node [change list]
  (assoc change :list list))

(def create (comp (partial zip/zipper branch? :list make-node) change))

(defn undo [history]
  (if-let [history (zip/up history)]
    history
    "Already at oldest change"))

(defn redo [history]
  (if-let [history (zip/down history)]
    history
    "Already at newest change"))

(defn commit
  ([history] (commit (-> history zip/node :value) history))
  ([value history]
     (-> history (zip/insert-child (change value)) zip/down)))
