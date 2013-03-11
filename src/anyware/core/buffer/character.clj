(ns anyware.core.buffer.character
  (:refer-clojure :exclude [pop])
  (:require [anyware.core.function :as function]
            [anyware.core.buffer :as buffer]))

(defn move
  ([field] (partial move field))
  ([field buffer]
     (if-let [char (buffer/peek field buffer)]
       (->> buffer
            (buffer/conj (buffer/inverse field) char)
            (buffer/pop field))
       buffer)))

(def forward (move :rights))

(def backward (move :lefts))

(def append (partial buffer/conj :lefts))

(defn pop [field]
  (function/safe (partial pop field)))

(def backspace (pop :lefts))

(def delete (pop :rights))
