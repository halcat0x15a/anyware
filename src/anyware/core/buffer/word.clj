(ns anyware.core.buffer.word
  (:refer-clojure :exclude [find next drop])
  (:require [anyware.core.buffer :as buffer]))

(def right #"^\W*\w+")

(def left #"\w+\W*$")

(defmulti regex identity)
(defmethod regex :rights [_] right)
(defmethod regex :lefts [_] left)

(defn find [field buffer]
  (->> buffer field (re-find (regex field)))) 

(defn move
  ([field] (partial move field))
  ([field buffer]
     (if-let [result (find field buffer)]
       (->> buffer
            (buffer/drop (count result) field)
            (buffer/conj (buffer/inverse field) result))
       buffer)))

(def next (move :rights))

(def prev (move :lefts))

(defn drop
  ([field] (partial drop field))
  ([field buffer]
     (if-let [result (find field buffer)]
       (buffer/drop (count result) field buffer)
       buffer)))

(def backspace (drop :lefts))

(def delete (drop :rights))
