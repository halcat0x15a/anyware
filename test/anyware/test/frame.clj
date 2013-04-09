(ns anyware.test.frame
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.zip :as zip]
            [anyware.test :as test]
            [anyware.core.frame :as frame]))

(defspec conj-window
  (fn [window frame]
    (->> frame (frame/conj window) zip/node))
  [^test/window window ^test/frame frame]
  (is (= % window)))

(defspec conj-find
  (fn [name value frame]
    (->> frame
         (frame/conj name value) (frame/find name)
         zip/node :value))
  [^string name ^anything value ^test/frame frame]
  (is (= % value)))
