(ns felis.syntax
  (:refer-clojure :exclude [comp or repeat])
  (:require [clojure.core :as core]
            [clojure.string :as string]))

(defrecord Input [source destination cursor])

(defn highlight [parser source]
  (let [{:keys [source destination]}
        (-> source (Input. "" 0) parser)]
    (str destination source)))

(defn extract [x]
  (cond (coll? x) (first x)
        (string? x) x))

(defn parser
  ([regex] (parser regex identity))
  ([regex transform]
     (fn [{:keys [cursor source destination] :as input}]
       (if-let [result (re-find regex source)]
         (let [length (-> result extract count)]
           (Input. (subs source length)
                   (->> result transform (str destination))
                   (+ cursor length)))
         input))))

(defn comp [parser & parsers]
  (fn [initial]
    (loop [{:keys [cursor] :as input} initial parsers (cons parser parsers)]
      (if (empty? parsers)
        input
        (let [[parser & parsers] parsers
              result (parser input)]
          (if (< cursor (:cursor result))
            (recur result parsers)
            initial))))))

(defn or [parser & parsers]
  (fn [{:keys [cursor] :as input}]
    (loop [parsers (cons parser parsers)]
      (if (empty? parsers)
        input
        (let [[parser & parsers] parsers
              result (parser input)]
          (if (< cursor (:cursor result))
            result
            (recur parsers)))))))

(defn repeat [parser]
  (fn [{:keys [cursor] :as input}]
    (let [result (parser input)]
      (if (< cursor (:cursor result))
        (recur result)
        result))))

(def default (parser #".*"))
