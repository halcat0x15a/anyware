(ns anyware.core.frame
  (:refer-clojure :exclude [find remove assoc])
  (:require [clojure.zip :as zip]))

(defrecord Entry [saved name value])

(def entry (partial ->Entry true))

(defn create [name value]
  (-> (entry name value) vector zip/vector-zip zip/down))

(defn find [name frame]
  (loop [frame (-> frame zip/root zip/vector-zip)]
    (cond (identical? name (-> frame zip/node :name)) frame
          (not (zip/end? frame)) (recur (zip/next frame)))))

(defn remove [frame]
  (if (-> frame zip/node :saved)
    (zip/remove frame)))

(defn assoc [name value frame]
  (if-let [frame (find name frame)]
    (if-let [frame (remove frame)]
      (-> frame (zip/insert-right (entry name value)) zip/right))))
