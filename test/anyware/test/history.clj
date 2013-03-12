(ns anyware.test.history
  (:require [clojure.test.generative :refer (defspec is)]
            [anyware.test :as test]
            [anyware.core.lens :as lens]
            [anyware.core.history :as history]))

(def lens (lens/comp :value lens/zip))

(defspec commit-undo
  (fn [history buffer]
    (->> history
         (history/commit buffer)
         history/undo
         (lens/get lens)))
  [^test/history history ^test/buffer buffer]
  (is (= % (lens/get lens history))))

(defspec commit-undo-redo
  (fn [history buffer]
    (->> history
         (history/commit buffer)
         history/undo
         history/redo
         (lens/get lens)))
  [^test/history history ^test/buffer buffer]
  (is (= % buffer)))
