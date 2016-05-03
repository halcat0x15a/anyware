(ns anyware.window
  (:require [clojure.string :as string]
            [anyware.buffer :as buffer]))

(defrecord Window [key x y width height])

(defn html [buffers window]
  (let [buffer (get buffers (:key window))
        lines (string/split (buffer/text buffer) \newline)
        length (count lines)]
    (subvec (vec (map (fn [line] (let [length (count line)] (subs line (min (:x window) length) (min (:width window) length)))) lines)) (min (:y window) length) (min (:height window) length))))
