(ns anyware.core.list
  (:refer-clojure :exclude [find remove assoc])
  (:require [clojure.zip :as zip]))

(defrecord Entry [saved name history])

(def entry (partial ->Entry true))

(defn create [name history]
  (-> (entry name history) vector zip/vector-zip zip/down))

(defn find [name list]
  (loop [list (-> list zip/root zip/vector-zip)]
    (cond (identical? name (-> list zip/node :name)) list
          (not (zip/end? list)) (recur (zip/next list)))))

(defn remove [list]
  (if (-> list zip/node :saved)
    (zip/remove list)))

(defn assoc [name history list]
  (if-let [list (find name list)]
    (if-let [list (remove list)]
      (-> list (zip/insert-right (entry name history)) zip/right))))
