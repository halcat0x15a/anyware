(ns anyware.core.buffer.history
  (:refer-clojure :exclude [read])
  (:require [clojure.zip :as zip]
            [anyware.core.buffer :as buffer]
            [anyware.core.lens.record :as record])
  (:import anyware.core.buffer.Buffer))

(defprotocol History
  (branch? [history])
  (children [history])
  (make-node [history list]))

(defrecord Change [list ^Buffer buffer]
  History
  (branch? [_] true)
  (children [_] list)
  (make-node [change list]
    (assoc change :list list)))

(def change (partial ->Change []))

(def create
  (comp (partial zip/zipper branch? children make-node) change))

(def read (comp create buffer/read))

(def undo (record/with record/history (record/safe zip/up)))

(def redo (record/with record/history (record/safe zip/down)))

(defn commit [^Buffer buffer ^Change history]
  (-> history (zip/insert-child (change buffer)) zip/down))
