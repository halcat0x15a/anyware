(ns anyware.test.buffer
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]
            [anyware.core.buffer.line :as line]
            [anyware.core.buffer.word :as word]))

(defn field []
  (gen/rand-nth [:lefts :rights]))

(defn move []
  (gen/rand-nth [character/move line/move word/move buffer/move]))

(defspec inverse-inverse
  (fn [field]
    (-> field buffer/inverse buffer/inverse))
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
  (is (= % (-> buffer buffer/begin))))

(defspec conj-drop
  (fn [buffer field string]
    (->> buffer
         (buffer/conj field string)
         (buffer/drop (count string) field)))
  [^test/buffer buffer ^{:tag `field} field ^string string]
  (is (= % buffer)))

(defspec move-cursor
  (fn [move field buffer] (move field buffer))
  [^{:tag `move} move ^{:tag `field} field ^test/buffer buffer]
  (is (<= (buffer/cursor field %) (buffer/cursor field buffer))))
