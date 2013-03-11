(ns anyware.test.buffer
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]
            [anyware.core.buffer.line :as line]
            [anyware.core.buffer.word :as word]))

(defn move []
  (gen/rand-nth [character/next character/prev
                 line/next line/prev
                 line/end buffer/begin
                 buffer/end buffer/begin]))

(defn field []
  (gen/rand-nth [:lefts :rights]))

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

(defspec write-read-write
  (fn [buffer]
    (->> buffer buffer/write buffer/read))
  [^test/buffer buffer]
  (is (= % (-> buffer buffer/begin))))

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
    (->> buffer (character/move field) (buffer/cursor field)))
  [^test/buffer buffer ^{:tag `field} field]
  (is (<= % (buffer/cursor field buffer))))

(defspec most-peek
  (fn [buffer field]
    (->> buffer
         (buffer/move field)
         (buffer/peek field)))
  [^test/buffer buffer ^{:tag `field} field]
  (is (nil? %)))

(defspec add-newline
  (fn [buffer field]
    (->> buffer
         (line/conj field)
         (buffer/peek field)))
  [^test/buffer buffer ^{:tag `field} field]
  (is (= % \newline)))
