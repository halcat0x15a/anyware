(ns anyware.core.parser.ast
  (:refer-clojure :exclude [map])
  (:require [clojure.zip :as zip]
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

(defn- make-node [_ children] children)

(def zip (partial zip/zipper branch? children make-node))

(defn cursor [n zipper]
  (let [node (zip/node zipper)]
    (cond (branch? node) (recur n (zip/next zipper))
          (not (pos? n)) zipper
          :else (recur (dec n) (zip/next zipper)))))
