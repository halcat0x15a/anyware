(ns anyware.test.buffer
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.buffer :as buffer]))

(defn move []
  (gen/rand-nth [buffer/right buffer/left
                 buffer/down buffer/up
                 buffer/head buffer/tail
                 buffer/begin buffer/end]))

(defn field []
  (gen/rand-nth [:lefts :rights]))

(defspec invert-invert
  (fn [field]
    (-> field buffer/invert buffer/invert))
  [^{:tag `field} field]
  (is (= % field)))

(defspec write-read
  (fn [string]
    (->> string buffer/read buffer/write))
  [^string string]
  (is (= % string)))

(defspec read-write
  (fn [buffer]
    (->> buffer buffer/write buffer/read))
  [^test/buffer buffer]
  (is (= % (buffer/begin buffer))))

(defspec constant
  #(% %2)
  [^{:tag `move} move ^test/buffer buffer]
  (is (= (buffer/write %)
         (buffer/write buffer))))

(defspec conj-pop
  (fn [buffer field char]
    (->> buffer
         (buffer/conj field char)
         (buffer/pop field)))
  [^test/buffer buffer ^{:tag `field} field ^char char]
  (is (= % buffer)))

(defspec move-cursor
  (fn [buffer field]
    (->> buffer (buffer/move field) (buffer/cursor field)))
  [^test/buffer buffer ^{:tag `field} field]
  (is (<= % (buffer/cursor field buffer))))

(defspec most-peek
  (fn [buffer field]
    (->> buffer
         (buffer/most field)
         (buffer/peek field)))
  [^test/buffer buffer ^{:tag `field} field]
  (is (nil? %)))
