(ns anyware.core.style
  (:refer-clojure :exclude [read]))

(def global
  (atom {:color "black"
         :background-color "white"
         :font-size "16px"
         :font-family "monospace"}))

(defrecord Color [foreground background])

(def color
  (atom {:cursor (Color. "white" "black")
         :special (Color. "magenta" "white")
         :symbol (Color. "blue" "white")
         :string (Color. "red" "white")
         :keyword (Color. "cyan" "white")}))

(defn read [key]
  (get @color key (Color. (:color @global) (:background-color @global))))
