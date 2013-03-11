(ns anyware.core.buffer.history
  (:refer-clojure :exclude [read empty])
  (:require [clojure.zip :as zip]
            [anyware.core.function :as function]
            [anyware.core.buffer :as buffer]))

(defprotocol History
  (branch? [history])
  (children [history])
  (make-node [history list]))

(defrecord Change [list buffer]
  History
  (branch? [_] true)
  (children [_] list)
  (make-node [change list]
    (assoc change :list list)))

(def change (partial ->Change []))

(def create
  (comp (partial zip/zipper branch? children make-node) change))

(def read (comp create buffer/read))

(def empty (read ""))

(def undo (function/safe zip/up))

(def redo (function/safe zip/down))

(defn commit [buffer history]
  (-> history (zip/insert-child (change buffer)) zip/down))
