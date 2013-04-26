(ns anyware.core.api.format.color
  (:refer-clojure :exclude [read]))

(defrecord Color [foreground background])

(def default (atom (Color. "black" "white")))

(def color
  (atom {:cursor (Color. "white" "black")
         :special (Color. "magenta" "white")
         :symbol (Color. "blue" "white")
         :string (Color. "red" "white")
         :keyword (Color. "cyan" "white")}))

(defn read [key]
  (get @color key @default))
