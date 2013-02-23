(ns anyware.test.history
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.test :refer [deftest testing with-test are]]
            [anyware.test :as test]
            [anyware.buffer :as buffer]
            [anyware.history :as history]))

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
