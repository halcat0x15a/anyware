(ns anyware.core.buffer.list
  (:refer-clojure :exclude [name read])
  (:require [clojure.zip :as zip]
            [anyware.core.buffer.history :as history]))

(defrecord Entry [name history])

(def name (atom "*scratch*"))

(def create (comp zip/down zip/vector-zip vector ->Entry))

(def read (comp (partial create @name) history/read))

(defn add [name history list]
  (-> list
      (zip/insert-right (Entry. name history))
      zip/right))
