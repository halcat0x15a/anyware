(ns anyware.core.history
  (:refer-clojure :exclude [read empty])
  (:require [clojure.zip :as zip]
            [anyware.core.function :as function]))

(defrecord Change [list value])

(def branch? (partial instance? Change))

(defn- make-node [change list]
  (assoc change :list list))

(def change (partial ->Change []))

(def create
  (comp (partial zip/zipper branch? :list make-node) change))

(def undo (function/safe zip/up))

(def redo (function/safe zip/down))

(defn commit [value history]
  (-> history (zip/insert-child (change value)) zip/down))
