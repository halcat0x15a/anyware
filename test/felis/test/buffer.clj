(ns felis.test.buffer
  (:require [clojure.test.generative :refer :all]
            [felis.test :as test]
            [felis.buffer :as buffer]
            [felis.edit :as edit]))

(defn insert-newline [buffer]
  (update-in buffer [:focus] #(edit/add % :lefts \newline)))

(defspec break
  (fn [buffer] (-> buffer buffer/break buffer/write))
  [^test/buffer buffer]
  (is (= % (-> buffer insert-newline buffer/write))))
