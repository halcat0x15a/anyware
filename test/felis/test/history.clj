(ns felis.test.history
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.test :refer [deftest testing with-test are]]
            [felis.test :as test]
            [felis.buffer :as buffer]
            [felis.history :as history]))

(defspec commit-undo
  (fn [history buffer]
    (->> history (history/commit buffer) history/undo :present))
  [^test/history history ^test/buffer buffer]
  (is (= % (:present history))))

(defspec commit-undo-redo
  (fn [history buffer]
    (->> history
         (history/commit buffer)
         history/undo
         history/redo
         :present))
  [^test/history history ^test/buffer buffer]
  (is (= % buffer)))
