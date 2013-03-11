(ns anyware.core.buffer.word
  (:require [anyware.core.buffer :as buffer]))

(defmulti regex identity)
(defmethod regex :rights [_] #"^\s*\w+")
(defmethod regex :lefts [_] #"\w+\s*$")

(defn move
  ([field] (partial move field))
  ([field buffer]
     (if-let [result (->> buffer field (re-find (regex field)))]
       (->> buffer
            (buffer/drop (count result) field)
            (buffer/conj (buffer/inverse field) result))
       buffer)))

(def forward (move :rights))

(def backward (move :lefts))
