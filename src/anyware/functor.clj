(ns anyware.functor
  (:refer-clojure :exclude [map]))

(defprotocol Functor
  (map [this f]))
