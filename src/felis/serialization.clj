(ns felis.serialization)

(defprotocol Serializable
  (serialize [serializable]))

(defmulti deserialize (fn [serializable string] serializable))
