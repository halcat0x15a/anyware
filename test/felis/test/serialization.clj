(ns felis.test.serialization
  (:refer-clojure :exclude [read])
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [felis.text :as text]
            [felis.buffer :as buffer]))

(defprotocol Serializable
  (write [serializable])
  (read [serializable string]))

(extend-protocol Serializable
  felis.text.Text
  (write [text] (text/write text))
  (read [_ string] (text/read string))
  felis.buffer.Buffer
  (write [buffer] (buffer/write buffer))
  (read [_ string] (buffer/read string)))

(defn text []
  (assoc text/default :rights (gen/string)))

(defn buffer []
  (assoc buffer/default
    :focus (text)
    :rights (apply list (gen/list text))))

(defn serializable []
  ((gen/rand-nth [text buffer])))

(defspec write-read
  (fn [string serializable]
    (->> string (read serializable) write))
  [^string string ^{:tag `serializable} serializable]
  (is (= % string)))

(defspec read-write
  (fn [serializable]
    (->> serializable write (read serializable)))
  [^{:tag `serializable} serializable]
  (is (= % serializable)))
