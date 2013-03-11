(ns anyware.core.buffer.line
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

(def append (partial buffer/conj :lefts \newline))

(def insert (partial buffer/conj :rights \newline))

(def break (partial buffer/conj :lefts \newline))
