(ns anyware.test.buffer
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.buffer :as buffer]))

(defn field []
  (gen/rand-nth [:left :right]))

(defn unit []
  (gen/rand-nth [buffer/char buffer/line buffer/word buffer/buffer]))

(defspec inverse-inverse
  (fn [field]
    ((field buffer/inverse) buffer/inverse))
  [^{:tag `field} field]
  (is (= % field)))

(defspec read-write
  (fn [string]
    (->> string buffer/read buffer/write))
  [^string string]
  (is (= % string)))

(defspec write-read
  (fn [buffer]
    (->> buffer buffer/write buffer/read))
  [^test/buffer buffer]
  (is (= % (->> buffer (buffer/move buffer/buffer :left)))))

(defspec append-substring
  (fn [buffer field string]
    (->> buffer
         (buffer/append field string)
         (buffer/substring field (count string))))
  [^test/buffer buffer ^{:tag `field} field ^string string]
  (is (= % buffer)))

(defspec move-cursor
  buffer/move
  [^{:tag `unit} unit ^{:tag `field} field ^test/buffer buffer]
  (is (<= (buffer/cursor field %) (buffer/cursor field buffer))))
