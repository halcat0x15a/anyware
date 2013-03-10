(ns anyware.core.parser.ast
  (:refer-clojure :exclude [map])
  (:require [clojure.zip :as zip]
            [anyware.core.parser :as parser]))

(defrecord Node [label value])

(defn extract [{:keys [value] :as node}]
  (if value value node))

(defn- branch? [node]
  (let [node' (extract node)]
    (or (vector? node')
        (> (-> node' str count) 1))))

(defn- children [{:keys [value] :as node}]
  (if value (seq value) node))

(defn- make-node [_ children] children)

(def zip (partial zip/zipper branch? children make-node))

(defn cursor [n zipper]
  (let [node (zip/node zipper)]
    (cond (branch? node) (recur n (zip/next zipper))
          (not (pos? n)) zipper
          :else (recur (dec n) (zip/next zipper)))))

(defn map [label parser]
  (parser/map (partial ->Node label) parser))
