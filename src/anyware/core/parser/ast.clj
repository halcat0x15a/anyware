(ns anyware.core.parser.ast
  (:refer-clojure :exclude [map])
  (:require [anyware.core.parser :as parser]))

(defrecord Node [label value])

(defn map [label parser]
  (parser/map (partial ->Node label) parser))
