(ns anyware.core.parser
  (:refer-clojure :exclude [map and or repeat])
  (:require [anyware.core.parser.result :as result]))

(defn map [f parser]
  (fn [input] (result/map (parser input) f)))

(defn- extract [x]
  (cond (coll? x) (first x)
        (string? x) x))

(defn regex [regex']
  (fn [input]
    (if-let [result (re-find regex' input)]
      (result/->Success result (subs input (-> result extract count)))
      (result/->Failure regex' input))))

(defn literal [string]
  (fn [input]
    (let [length (count string)]
      (cond (< (count input) length) (result/->Failure length input)
            (= (subs input 0 length) string)
            (result/->Success string (subs input length))
            :else (result/->Failure string input)))))

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

(def success (partial result/->Success []))

(defn maybe [parser]
  (or parser success))
    
(def text (regex #"[\s\S]*"))
