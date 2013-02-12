(ns felis.test.serialization
  (:refer-clojure :exclude [read])
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [felis.serialization :as serialization]
            [felis.text :as text]
            [felis.buffer :as buffer]))

(defprotocol Deserializable
  (read [serializable string]))

(extend-protocol Deserializable
  felis.text.Text
  (read [_ string] (text/read string))
  felis.buffer.Buffer
  (read [_ string] (buffer/read string)))

(defn text []
  (assoc text/default :rights (gen/string)))

(defn buffer []
  (assoc buffer/default
    :focus (text)
    :bottoms (apply list (gen/list text))))

(defn serializable []
  ((gen/rand-nth [text buffer])))

(defspec write-read
  (fn [string serializable]
    (->> string (read serializable) serialization/write))
  [^string string ^{:tag `serializable} serializable]
  (is (= % string)))

(defspec read-write
  (fn [serializable]
    (->> serializable serialization/write (read serializable)))
  [^{:tag `serializable} serializable]
  (is (= % serializable)))
