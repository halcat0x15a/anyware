(ns anyware.core.canvas
  (:require [clojure.string :as string]
            [anyware.core.buffer :as buffer]
            [anyware.core.parser :refer :all]
            [anyware.core.util :as util]))

(defrecord Color [foreground background])

(def ^:dynamic *style*
  {:cursor (Color. "white" "black")
   :symbol (Color. "blue" "white")
   :string (Color. "maroon" "white")
   :keyword (Color. "aqua" "white")
   :special (Color. "magenta" "white")
   :default (Color. "black" "white")})

(def special
  {\< "&lt;"
   \> "&gt;"
   \& "&amp;"
   \newline " \n"})

(defn html [char {:keys [foreground background]}]
  (format "<span style=\"color:%s;background-color:%s;\">%s</span>"
          foreground
          background
          (get special char char)))

(defn cursor [display {:keys [left]}]
  (assoc display (count left) (:cursor *style*)))

(defn render
  ([display string]
     (reduce (fn [text [i color]]
               (str (subs text 0 i)
                    (html (nth text i) color)
                    (subs text (inc i))))
             string
             display))
  ([buffer y height]
     (let [string (str buffer \space)
           lines (util/split-lines string)
           top (->> lines (take y) string/join count)]
       (-> (sorted-map)
           (cursor buffer)
           (subseq > top)
           (render string)
           (subs top)))))
