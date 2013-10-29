(ns anyware.core.view
  (:require [clojure.string :as string]))

(defrecord View [x y width height])

(defn move [{:keys [y height] :as view} cursor]
  (let [y' (+ height y)]
    (assoc view
      :y (cond (<= y' cursor) (inc (- cursor height))
               (< cursor y) cursor
               :else y))))

(defn bounds [lines {:keys [y height]}]
  (->> lines
       (drop y)
       (take height)
       (string/join \newline)))
