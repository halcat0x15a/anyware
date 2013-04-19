(ns anyware.core.frame
  (:refer-clojure :exclude [find remove conj assoc])
  (:require [clojure.zip :as zip]))

(def create (comp zip/down zip/vector-zip vector))

(defn find
  ([name] (partial find name))
  ([name frame]
     (loop [frame (-> frame zip/root zip/vector-zip)]
       (cond (identical? name (-> frame zip/node :name)) frame
             (not (zip/end? frame)) (recur (zip/next frame))))))

(defn remove [frame]
  (if (-> frame zip/node :changed?)
    "No write since last change"
    (zip/remove frame)))

(defn conj [value frame]
  (-> frame (zip/insert-right value) zip/right))

(defn assoc
  ([f name value] (partial assoc name value))
  ([f name value frame]
     (if-let [frame (find name frame)]
       (let [frame (remove frame)]
         (if (string? frame)
           frame
           (conj (f name value) frame)))
       (conj (f name value) frame))))
