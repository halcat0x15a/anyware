(ns anyware.core.frame
  (:refer-clojure :exclude [find remove conj assoc set])
  (:require [clojure.zip :as zip]))

(defrecord Window [name value])

(defrecord Saved [name value])

(defn create
  ([name value] (create (Saved. name value)))
  ([window] (-> window vector zip/vector-zip zip/down)))

(defn find [name frame]
  (loop [frame (-> frame zip/root zip/vector-zip)]
    (cond (identical? name (-> frame zip/node :name)) frame
          (not (zip/end? frame)) (recur (zip/next frame)))))

(defn remove [frame]
  (if (->> frame zip/node (instance? Saved))
    (zip/remove frame)))

(defn conj
  ([name value frame] (conj (Saved. name value) frame))
  ([window frame]
     (-> frame (zip/insert-right window) zip/right)))

(defn assoc [name value frame]
  (if-let [frame (find name frame)]
    (if-let [frame (remove frame)]
      (conj name value frame))))

(defn save [frame]
  (zip/edit frame map->Saved))
