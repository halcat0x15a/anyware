(ns anyware.core.buffer.character
  (:refer-clojure :exclude [pop next])
  (:require [anyware.core.buffer :as buffer]))

(defn pop
  ([field] (partial pop field))
  ([field buffer]
     (if-not (-> buffer field empty?)
       (buffer/drop 1 field buffer))))

(defn move
  ([field] (partial move field))
  ([field buffer]
     (if-let [char (buffer/peek field buffer)]
       (->> buffer
            (buffer/conj (buffer/inverse field) char)
            (pop field)))))

(def next (move :rights))

(def prev (move :lefts))

(def append (partial buffer/conj :lefts))

(def backspace (pop :lefts))

(def delete (pop :rights))
