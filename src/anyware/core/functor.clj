(ns anyware.core.functor
  (:refer-clojure :exclude [map]))

(defprotocol Functor
  (map [this f]))
