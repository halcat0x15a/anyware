(ns anyware.test.frame
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.zip :as zip]
            [anyware.test :as test]
            [anyware.core.frame :as frame]))

(defspec conj-value
  (fn [value frame]
    (->> frame (frame/conj value) zip/node))
  [^anything value ^test/frame frame]
  (is (= % value)))
