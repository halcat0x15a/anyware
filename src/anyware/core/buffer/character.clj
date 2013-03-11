(ns anyware.core.buffer.character
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

(def backspace (partial (function/safe pop) :lefts))

(def delete (partial (function/safe pop) :rights))
