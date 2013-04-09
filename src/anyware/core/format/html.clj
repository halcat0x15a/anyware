(ns anyware.core.format.html
  (:refer-clojure :exclude [format])
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]
            [anyware.core.format :as format]
            [anyware.core.format.color :as color]))

(def global
  (atom {:color "black"
         :background-color "white"
         :font-size "16px"
         :font-family "monospace"}))

(defn escape [string]
  (string/escape string {\< "&lt;" \> "&gt;" \& "&amp;"}))

(defn- attribute [attributes key value]
  (str attributes \space (name key) \= \" value \"))

(defn element [label attributes content]
  (let [label (name label)]
    (str \< label (reduce-kv attribute "" attributes) \>
         content
         \< \/ label \>)))

(defn- declaration [declarations property value]
  (str declarations (name property) \: value \;))

(def style (partial reduce-kv declaration ""))

(def format
  (reify format/Format
    (root [this child]
      (element :pre {:class "editor" :style (style @global)} child))
    (node [this {:keys [label value]}]
      (let [{:keys [foreground background]} (-> label color/read)]
        (element :span
                 {:style (style {:color foreground
                                 :background-color background})}
                 (format/write this value))))
    (text [this text]
      (escape text))))
