(ns anyware.core.frame
  (:refer-clojure :exclude [next find remove conj assoc])
  (:require [clojure.zip :as zip]
            [anyware.core.function :refer (safe)]))

(defprotocol Window
  (save? [window]))

(defrecord Saved [name value]
  Window
  (save? [_] true))

(defrecord Modified [name value]
  Window
  (save? [_] false))

(defn create
  ([name value] (create (Saved. name value)))
  ([window] (-> window vector zip/vector-zip zip/down)))

(def next (safe zip/right))

(def prev (safe zip/left))

(defn find
  ([name] (partial find name))
  ([name frame]
     (loop [frame (-> frame zip/root zip/vector-zip)]
       (cond (identical? name (-> frame zip/node :name)) frame
             (not (zip/end? frame)) (recur (zip/next frame))))))

(defn remove [frame]
  (if (-> frame zip/node save?)
    (zip/remove frame)))

(defn conj
  ([name value frame] (conj (Saved. name value) frame))
  ([window frame]
     (-> frame (zip/insert-right window) zip/right)))

(defn assoc [name value frame]
  (if-let [frame' (find name frame)]
    (if-let [frame'' (remove frame')]
      (conj name value frame'')
      (conj name value frame'))
    (conj name value frame)))

(defn save [frame]
  (zip/edit frame map->Saved))
