(ns anyware.test.frame
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.zip :as zip]
            [anyware.test :as test]
            [anyware.core.frame :as frame]))

(defspec conj
  (fn [window frame]
    (->> frame (frame/conj window) zip/node))
  [^test/window window ^test/frame frame]
  (is (= % window)))
