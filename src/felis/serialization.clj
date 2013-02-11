(ns felis.serialization
  (:refer-clojure :exclude [read]))

(defprotocol Serializable
  (write [serializable]))

(defmulti read (fn [serializable string] serializable))
