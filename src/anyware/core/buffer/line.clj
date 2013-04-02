(ns anyware.core.buffer.line
  (:refer-clojure :exclude [while next conj pop remove])
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]))

(defn- while
  ([f] (partial while f))
  ([f field buffer]
     (if-let [char (buffer/peek field buffer)]
       (if (identical? char \newline)
         buffer
         (let [buffer' (f field buffer)]
           (if (identical? buffer' buffer)
             buffer
             (recur f field buffer'))))
       buffer)))

(def move (while character/move))

(def begin (partial move :lefts))

(def end (partial move :rights))

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

(def backspace (partial pop :lefts))

(def delete (partial pop :rights))

(def remove (comp backspace delete))
