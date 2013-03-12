(ns anyware.core.parser.ast
  (:refer-clojure :exclude [map])
  (:require [clojure.zip :as zip]
            [anyware.core.buffer :as buffer]
            [anyware.core.parser :as parser]))

(defrecord Node [label value])

(defn map [label parser]
  (parser/map (partial ->Node label) parser))

(defn extract [{:keys [value] :as node}]
  (if value value node))

(defn- branch? [node]
  (let [node (extract node)]
    (or (vector? node)
        (and (string? node)
             (< 1 (count node))))))

(defn- children [node]
  (-> node extract seq))

(defn- make-node [_ children] (vec children))

(def zip (partial zip/zipper branch? children make-node))

(defn traverse
  ([f] (partial traverse f))
  ([f n zipper]
     (let [node (zip/node zipper)]
       (cond (zip/end? zipper) zipper
             (branch? node) (recur f n (zip/next zipper))
             (not (pos? n)) zipper
             :else (recur f (dec n) (-> zipper f zip/next))))))

(def move (traverse identity))

(def drop (traverse zip/remove))

(defn parse [parser {:keys [lefts] :as buffer}]
  (let [{:keys [result next]} (->> buffer buffer/write parser)]
    (-> (->> result zip (move (count lefts)))
        (zip/edit (partial ->Node :cursor))
        zip/rightmost
        (zip/insert-right next))))
