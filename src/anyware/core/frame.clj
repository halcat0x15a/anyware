(ns anyware.core.frame
  (:refer-clojure :exclude [next find remove conj assoc])
  (:require [clojure.zip :as zip]))

(defrecord Window [saved? name value])

(def window (partial ->Window true))

(def create (comp zip/down zip/vector-zip vector))

(defn find
  ([name] (partial find name))
  ([name frame]
     (loop [frame (-> frame zip/root zip/vector-zip)]
       (cond (identical? name (-> frame zip/node :name)) frame
             (not (zip/end? frame)) (recur (zip/next frame))))))

(defn remove [frame]
  (if (-> frame zip/node :save?)
    (zip/remove frame)))

(defn conj [window frame]
  (-> frame (zip/insert-right window) zip/right))

(defn assoc
  ([name value] (partial assoc name value))
  ([name value frame]
     (if-let [frame (find name frame)]
       (if-let [frame (remove frame)]
         (conj (window name value) frame)
         frame)
       (conj (window name value) frame))))

(defn save [frame]
  (zip/edit frame #(assoc % :save? true)))
