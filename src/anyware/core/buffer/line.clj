(ns anyware.core.buffer.line
  (:refer-clojure :exclude [while next conj pop])
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]))

(defn while [f]
  (letfn [(while
            ([field] (partial while field))
            ([field buffer]
               (if-let [char (buffer/peek field buffer)]
                 (if (identical? char \newline)
                   buffer
                   (recur field (f field buffer)))
                 buffer)))]
    while))

(def move (while character/move))

(def begin (move :lefts))

(def end (move :rights))

(def next (comp character/next begin))

(def prev (comp character/prev end))

(def break (partial buffer/conj :lefts \newline))

(defn conj
  ([field] (partial conj field))
  ([field buffer]
     (->> buffer
          (move (buffer/inverse field))
          (buffer/conj field \newline))))

(def append (conj :lefts))

(def insert (conj :rights))

(def pop (while character/pop))

(def backspace (pop :lefts))

(def delete (pop :rights))
