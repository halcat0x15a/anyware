(ns anyware.core.list
  (:refer-clojure :exclude [name])
  (:require [clojure.zip :as zip]))

(defrecord Entry [name history])

(def default (atom "*scratch*"))

(defn create
  ([history] (create @default history))
  ([name history]
     (-> (Entry. name history)
         vector
         zip/vector-zip
         zip/down)))

(defn add [name history list]
  (-> list
      (zip/insert-right (Entry. name history))
      zip/right))
