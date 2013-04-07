(ns anyware.core.parser
  (:refer-clojure :exclude [map and or repeat])
  (:require [anyware.core.parser.result :as result]))

(def success (partial result/->Success []))

(def failure (partial result/->Failure []))

(defn map [f parser]
  (fn [input] (result/map (parser input) f)))

(defn- extract [x]
  (cond (coll? x) (first x)
        (string? x) x))

(defn regex [regex]
  (fn [input]
    (if-let [result (re-find regex input)]
      (result/->Success result (subs input (-> result extract count)))
      (failure input))))

(defn literal [string]
  (fn [input]
    (let [length (count string)]
      (cond (< (count input) length) (failure input)
            (= (subs input 0 length) string)
            (result/->Success string (subs input length))
            :else (failure input)))))

(defn or [parser & parsers]
  (fn [input]
    (reduce (fn [result parser]
              (result/or result (parser input)))
            (parser input)
            parsers)))

(defn and [parser & parsers]
  (fn [input]
    (reduce (fn [result parser]
              (result/mapcat
               result
               (fn [result input]
                 (result/map (parser input)
                             (partial conj result)))))
            (result/map (parser input) vector)
            parsers)))

(defn repeat [parser]
  (fn [input]
    (loop [result (result/map (parser input) vector)]
      (let [result' (result/map (-> result :next parser)
                                (partial conj (:result result)))]
        (if (result/success? result')
          (recur result')
          (result/or result (result/->Success [] input)))))))

(defn maybe [parser]
  (or parser success))

(defn id [s] (result/->Success s ""))
