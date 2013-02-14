(ns felis.test.buffer
  (:require [clojure.test.generative :refer :all]
            [felis.test :as test]
            [felis.serialization :as serialization]
            [felis.edit :as edit]
            [felis.buffer :as buffer]))

(defn insert-newline [buffer]
  (update-in buffer [:focus] (partial edit/insert \newline :lefts)))

(defspec break
  (fn [buffer] (-> buffer buffer/break serialization/write))
  [^test/buffer buffer]
  (is (= % (-> buffer insert-newline serialization/write))))
