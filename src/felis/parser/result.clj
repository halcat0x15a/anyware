(ns felis.parser.result
  (:refer-clojure :exclude [map mapcat or]))

(defprotocol Result
  (success? [result])
  (map [result f])
  (mapcat [result f])
  (or [result result']))

(defrecord Success [result next]
  Result
  (success? [success] true)
  (map [success f]
    (assoc success
      :result (f result)))
  (mapcat [success f] (f result next))
  (or [success result] success))

(defrecord Failure [message next]
  Result
  (success? [failure] false)
  (map [failure f] failure)
  (mapcat [failure f] failure)
  (or [failure result] result))
