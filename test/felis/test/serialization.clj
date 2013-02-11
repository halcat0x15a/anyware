(ns felis.test.serialization
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [felis.text :as text]
            [felis.buffer :as buffer]
            [felis.serialization :as serialization]))

(defn serializable []
  (letfn [(text [] (assoc text/default :rights (gen/string)))]
    (gen/rand-nth
     [(text) (assoc buffer/default
               :focus (text)
               :rights (apply list (gen/list text)))])))

(defspec write-read
  (fn [string serializable]
    (->> string
         (serialization/read (type serializable))
         serialization/write))
  [^string string ^{:tag `serializable} serializable]
  (is (= % string)))

(defspec read-write
  (fn [serializable]
    (->> serializable
         serialization/write
         (serialization/read (type serializable))))
  [^{:tag `serializable} serializable]
  (is (= % serializable)))
