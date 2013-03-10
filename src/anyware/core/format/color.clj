(ns anyware.core.format.color
  (:refer-clojure :exclude [read]))

(def default (atom :black))

(def color
  (atom {:special :magenta
         :symbol :blue
         :string :red
         :keyword :cyan}))

(defn read [key]
  (get @color key @default))
