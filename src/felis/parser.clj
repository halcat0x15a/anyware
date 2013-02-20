(ns felis.parser
  (:refer-clojure :exclude [map comp and or repeat])
  (:require [clojure.string :as string]
            [felis.parser.result :as result]))

(defn extract [x]
  (cond (coll? x) (first x)
        (string? x) x))

(defn regex [regex]
  (fn [input]
    (if-let [result (re-find regex input)]
      (result/->Success result (subs input (-> result extract count)))
      (result/->Failure (str "string matching regex "
                             regex
                             " expected but nil found")
                        input))))

(defn literal [literal]
  (fn [input]
    (let [size (count literal)]
      (if (<= size (count input))
        (let [result (subs input 0 size)]
          (if (= literal result)
            (result/->Success result (subs input size))
            (result/->Failure (str literal " expected but " result " found") input)))
        (result/->Failure (str "string index out of range " size) input)))))

(defn map [parser f]
  (if (fn? parser)
    (fn [input] (result/map (parser input) f))
    (result/map parser f)))

(defn or [parser parser' & parsers]
  (fn [input]
    (loop [result (parser input)
           parsers (cons parser' parsers)]
      (if (empty? parsers)
        result
        (recur (result/or result ((first parsers) input))
               (rest parsers))))))

(defn and [parser & parsers]
  (fn [input]
    (let [result (result/map (parser input) vector)]
      (loop [result result
             parsers parsers]
        (if (empty? parsers)
          result
          (recur (result/mapcat result
                                (fn [result input]
                                  (result/map ((first parsers) input)
                                              (partial conj result))))
                 (rest parsers)))))))
                     
(defn repeat [parser]
  (fn [input]
    (let [result (result/map (parser input) vector)]
      (loop [result result]
        (let [result' (result/map (parser (:next result))
                                  (partial conj (:result result)))]
          (if (result/success? result')
            (recur result')
            (result/or result (result/->Success [] input))))))))
