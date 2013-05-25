(ns anyware.core.tree
  (:refer-clojure :exclude [map])
  (:require [clojure.zip :as zip]
            [anyware.core.buffer :as buffer]
            [anyware.core.parser :as parser]))

(defprotocol Node
  (branch? [node])
  (children [node]))

(extend-protocol Node
  java.lang.Character
  (branch? [node] false)
  (children [node])
  java.lang.String
  (branch? [node] true)
  (children [node] (seq node))
  clojure.lang.IPersistentVector
  (branch? [node] true)
  (children [node] (seq node))
  clojure.lang.IPersistentMap
  (branch? [node] false)
  (children [node] (-> node first val)))

(defn map [label parser]
  (parser/map (partial hash-map label) parser))

(def zip (partial zip/zipper branch? children #(vec %2)))

(defn move [tree n]
  (cond (zip/end? tree) tree
        (zip/branch? tree) (recur (zip/next tree) n)
        (not (pos? n)) tree
        :else (recur (zip/next tree) (dec n))))

(defn cursor [tree]
  (-> (if (zip/end? tree)
        (-> tree (assoc 1 \space) zip zip/down zip/rightmost)
        tree)
      (zip/edit (partial hash-map :cursor))))

(defn parse [{:keys [left] :as buffer} parser]
  (let [{:keys [result next]} (->> buffer buffer/write parser)]
    (-> result
        zip
        (move (count left))
        cursor
        zip/rightmost
        (zip/insert-right next)
        zip/root)))
