(ns anyware.core.html
  (:require [anyware.core.buffer :as buffer]))

(def ^:dynamic *stack*)

(defrecord Range [start end color])

(defn cursor [buffer]
  (let [{:keys [buffer cursor]} (buffer/view buffer)]
    (set! *stack* (conj *stack* (Range. cursor (inc cursor) :reverse)))))

(defn render [{:keys [buffer]}]
  (binding [*stack* []]
    (cursor buffer)
    (str "<pre>" buffer "</pre>")))
