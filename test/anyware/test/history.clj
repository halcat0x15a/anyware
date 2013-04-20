(ns anyware.test.history
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.zip :as zip]
            [anyware.test :as test]
            [anyware.core.record :as record]
            [anyware.core.history :as history]))

(def lens (record/comp :current record/zip))

(defspec commit-undo
  (fn [history buffer]
    (->> history
         (history/commit buffer)
         history/undo
         (record/get lens)))
  [^test/history history ^test/buffer buffer]
  (is (= % (record/get lens history))))

(defspec commit-undo-redo
  (fn [history buffer]
    (->> history
         (history/commit buffer)
         history/undo
         history/redo
         (record/get lens)))
  [^test/history history ^test/buffer buffer]
  (is (= % buffer)))
