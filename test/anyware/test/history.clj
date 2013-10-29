(ns anyware.test.history
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.zip :as zip]
            [anyware.test :as test]
            [anyware.core.history :as history]))

(def current [0 :current])

(defspec commit-undo
  (fn [history buffer]
    (get-in (->> history
                 (history/commit buffer)
                 history/undo)
            current))
  [^test/history history ^test/buffer buffer]
  (is (= % (get-in history current))))

(defspec commit-undo-redo
  (fn [history buffer]
    (get-in (->> history
                 (history/commit buffer)
                 history/undo
                 history/redo)
             current))
  [^test/history history ^test/buffer buffer]
  (is (= % buffer)))
