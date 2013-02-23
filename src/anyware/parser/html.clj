(ns anyware.parser.html
  (:refer-clojure :exclude [< seq])
  (:require [anyware.parser :as parser]
            [anyware.html :as html]))

(defn < [class parser]
  (parser/map (partial html/< :span {:class class}) parser))

(def seq (partial parser/map (partial apply html/seq)))
