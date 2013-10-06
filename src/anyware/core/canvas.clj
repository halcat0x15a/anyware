(ns anyware.core.canvas
  (:require [clojure.string :as string]))

(def ^:dynamic *display*)

(defrecord Color [foreground background])

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

(defn render [renderer string]
  (areduce *display* i text ""
           (let [char (nth string i)]
             (str text (if-let [color (aget *display* i)]
                         (renderer char color)
                         char)))))
