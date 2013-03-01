(ns anyware.test.history
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.zip :as zip]
            [anyware.test :as test]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]))

(defspec commit-undo
  (fn [history buffer]
    (->> history (history/commit buffer) zip/up zip/node :buffer))
  [^test/history history ^test/buffer buffer]
  (is (= % (-> history zip/node :buffer))))

(defspec commit-undo-redo
  (fn [history buffer]
    (->> history
         (history/commit buffer)
         zip/up
         zip/down
         zip/node
         :buffer))
  [^test/history history ^test/buffer buffer]
  (is (= % buffer)))
