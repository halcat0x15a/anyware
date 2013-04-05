(ns anyware.core.frame
  (:refer-clojure :exclude [find remove assoc set])
  (:require [clojure.zip :as zip]))

(defrecord Window [name value])

(defrecord Saved [name value])

(def create (comp zip/down zip/vector-zip vector ->Saved))

(defn find [name frame]
  (loop [frame (-> frame zip/root zip/vector-zip)]
    (cond (identical? name (-> frame zip/node :name)) frame
          (not (zip/end? frame)) (recur (zip/next frame)))))

(defn remove [frame]
  (if (->> frame zip/node (instance? Saved))
    (zip/remove frame)))

(defn assoc [name value frame]
  (if-let [frame (find name frame)]
    (if-let [frame (remove frame)]
      (-> frame (zip/insert-right (Saved. name value)) zip/right))))

(defn save [frame]
  (zip/edit frame map->Saved))