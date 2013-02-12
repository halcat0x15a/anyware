(ns felis.serialization)

(defprotocol Serializable
  (write [this]))
