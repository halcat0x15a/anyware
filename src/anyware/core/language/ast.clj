(ns anyware.core.language.ast
  (:refer-clojure :exclude [map drop])
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
       (cond (-> zipper zip/next zip/end?) zipper
             (branch? node) (recur f n (zip/next zipper))
             (not (pos? n)) zipper
             :else (recur f (dec n) (-> zipper f zip/next))))))

(def move (traverse identity))

(def drop (traverse zip/remove))

(defn parse [{:keys [left] :as buffer}]
  (if-let [parser (-> buffer meta :parser)]
    (let [{:keys [result next]} (->> buffer buffer/write parser)]
      (-> (->> result zip (move (count left)))
          (zip/edit (partial ->Node :cursor))
          zip/rightmost
          (zip/insert-right next)
          zip/root))
    (buffer/write buffer)))
