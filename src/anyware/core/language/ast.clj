(ns anyware.core.language.ast
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

(defn move [n zipper]
  (let [node (zip/node zipper)
        next (-> zipper zip/next (with-meta (meta zipper)))]
    (cond (branch? node) (recur n next)
          (not (pos? n)) zipper
          (zip/end? next) (with-meta next {:over true})
          :else (recur (dec n) next))))

(defn cursor [zipper]
  (-> (if (-> zipper meta :over)
        (-> zipper zip/node (conj \space) zip zip/down zip/rightmost)
        zipper)
      (zip/edit (partial ->Node :cursor))))

(defn parse [{:keys [left] :as buffer} parser]
  (let [{:keys [result next]} (->> buffer buffer/write parser)]
    (-> (->> result vector zip (move (count left)))
        cursor
        zip/rightmost
        (zip/insert-right next)
        zip/root)))
