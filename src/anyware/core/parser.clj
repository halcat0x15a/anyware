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
    (loop [result (parser input) [parser & parsers] parsers]
      (if parser
        (recur (result/or result (parser input)) parsers)
        result))))

(defn and [parser & parsers]
  (fn [input]
    (loop [result (result/map (parser input) vector)
           [parser & parsers] parsers]
      (if parser
        (recur (result/mapcat
                result
                (fn [result input]
                  (result/map (parser input)
                              (partial conj result))))
               parsers)
        result))))
                     
(defn repeat [parser]
  (fn [input]
    (loop [result (result/map (parser input) vector)]
      (let [result' (result/map (-> result :next parser)
                                (partial conj (:result result)))]
        (if (result/success? result')
          (recur result')
          (result/or result (result/->Success [] input)))))))

(def success
  (fn [input]
    (result/->Success "" input)))

(defn maybe [parser]
  (or parser success))

(defn parse [parser source]
  (-> source parser :result))
    
(def text (regex #"[\s\S]*"))
