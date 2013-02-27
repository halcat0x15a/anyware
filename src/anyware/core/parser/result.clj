(ns anyware.core.parser.result
  (:refer-clojure :exclude [map mapcat or]))

(defprotocol Result
  (success? [result])
  (mapcat [result f])
  (or [result result']))

(defrecord Success [result next]
  Result
  (success? [success] true)
  (mapcat [success f] (f result next))
  (or [success result] success))

(defrecord Failure [message next]
  Result
  (success? [failure] false)
  (mapcat [failure f] failure)
  (or [failure result] result))

(defn map [result f]
  (mapcat result
          (fn [result next]
            (Success. (f result) next))))
