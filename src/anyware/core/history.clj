(ns anyware.core.history
  (:require [clojure.zip :as zip]))

(defrecord Change [list value])

(def change (partial ->Change []))

(def branch? (partial instance? Change))

(defn- make-node [change list]
  (assoc change :list list))

(def create (comp (partial zip/zipper branch? :list make-node) change))

(defn commit [value history]
  (-> history (zip/insert-child (change value)) zip/down))
