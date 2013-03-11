(ns anyware.core.buffer.line
  (:refer-clojure :exclude [conj])
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]))

(defn move
  ([field] (partial move field))
  ([field buffer]
     (if-let [char (buffer/peek field buffer)]
       (if (identical? char \newline)
         buffer
         (recur field (character/move field buffer)))
       buffer)))

(def begin (move :lefts))

(def end (move :rights))

(def forward (comp character/forward begin))

(def backward (comp character/backward end))

(def break (partial buffer/conj :lefts \newline))

(defn conj
  ([field] (partial conj field))
  ([field buffer]
     (->> buffer
          (move (buffer/inverse field))
          (buffer/conj field \newline))))

(def append (conj :lefts))

(def insert (conj :rights))
