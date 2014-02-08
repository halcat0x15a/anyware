(ns anyware.core.view
  (:require [clojure.string :as string]))

(defrecord View [x y width height])

(defn move [{:keys [y height] :as view} cursor]
  (let [y' (+ height y)]
    (assoc view
      :y (cond (<= y' cursor) (inc (- cursor height))
               (<= cursor y) cursor
               :else y))))

(defn bounds [buffer {:keys [y]}]
  (subs buffer
        (loop [n 0 y y]
          (cond (zero? y) n
                (identical? (nth buffer n) \newline) (recur (inc n) (dec y))
                :else (recur (inc n) y)))))
