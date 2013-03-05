(ns anyware.core.buffer.list
  (:refer-clojure :exclude [read])
  (:require [clojure.zip :as zip]
            [anyware.core.buffer.history :as history])
  (:import anyware.core.buffer.history.History))

(defrecord Entry [name ^History history])

(def create (comp zip/down zip/vector-zip vector ->Entry))

(def read (comp create history/read))

(defn add [^String name ^History history list]
  (-> list
      (zip/insert-right (Entry. name history))
      zip/right))
