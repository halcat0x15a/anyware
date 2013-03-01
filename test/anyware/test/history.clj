(ns anyware.test.history
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.zip :as zip]
            [anyware.test :as test]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]))

(defspec commit-undo
  (fn [history buffer]
    (->> history (history/commit buffer) history/undo zip/node))
  [^test/history history ^test/buffer buffer]
  (is (= % (zip/node history))))

(defspec commit-undo-redo
  (fn [history buffer]
    (->> history
         (history/commit buffer)
         history/undo
         history/redo
         zip/node))
  [^test/history history ^test/buffer buffer]
  (is (= % buffer)))

(defspec not-branch
  history/create
  [^test/buffer buffer]
  (is (= (zip/node %) buffer)))
