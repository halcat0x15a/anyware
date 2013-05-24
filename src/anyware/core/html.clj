(ns anyware.core.html
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [anyware.core.keys :as keys]
            [anyware.core.api :as api]
            [anyware.core.buffer :as buffer]
            [anyware.core.language.ast :as ast]
            [anyware.core.editor :as editor]
            [anyware.core.style :as style]))

(def special
  {\< "&lt;"
   \> "&gt;"
   \& "&amp;"
   \newline " \n"})

(defn escape [string] (string/escape string special))

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

(declare show)

(defn node [{:keys [label value]}]
  (let [{:keys [foreground background]} (style/read label)]
    (element :span
             {:style (style {:color foreground
                             :background-color background})}
             (show value))))

(defn show [x]
  (cond (:label x) (node x)
        (vector? x) (reduce str (mapv show x))
        :else (escape (str x))))

(defn render [editor]
  (element :pre {:class "editor" :style (style @style/global)}
           (str (-> editor
                    (get-in keys/minibuffer)
                    buffer/write
                    escape)
                \newline
                (-> editor
                    (get-in keys/buffer)
                    (ast/parse (-> editor
                                   (get-in keys/window)
                                   meta
                                   :parser))
                    show))))
