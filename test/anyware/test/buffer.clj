(ns anyware.test.buffer
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.buffer :as buffer]))

(defn field []
  (gen/rand-nth [:left :right]))

(defn unit []
  (gen/rand-nth [first buffer/line buffer/word identity]))

(defspec double-inverse
  (fn [field] (buffer/inverse (buffer/inverse field)))
  [^{:tag `field} field]
  (is (= % field)))

(defspec read-write
  (fn [string]
    (->> string buffer/read buffer/write))
  [^string string]
  (is (= % string)))

(defspec insert-substring
  (fn [buffer field string]
    (->> buffer
         (buffer/insert string field)
         (buffer/substring (count string) field)))
  [^test/buffer buffer ^{:tag `field} field ^string string]
  (is (= % buffer)))

(defspec move-cursor
  buffer/move
  [^{:tag `unit} unit ^{:tag `field} field ^test/buffer buffer]
  (is (<= (buffer/cursor field %) (buffer/cursor field buffer))))

(defspec select-and-copy
  (fn [unit field buffer]
    [(->> buffer
          buffer/select
          (buffer/move unit field)
          buffer/copy
          str)
     (-> buffer field unit str)])
  [^{:tag `unit} unit ^{:tag `field} field ^test/buffer buffer]
  (is (apply = %)))
