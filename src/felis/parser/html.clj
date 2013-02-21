(ns felis.parser.html
  (:refer-clojure :exclude [< seq])
  (:require [felis.parser :as parser]
            [felis.html :as html]))

(defn < [class parser]
  (parser/map (partial apply html/< :span {:class class}) parser))

(def seq (partial parser/map (partial apply html/seq)))
