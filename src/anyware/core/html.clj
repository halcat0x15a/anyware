(ns anyware.core.html
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [anyware.core.keys :as keys]
            [anyware.core.api :as api]
            [anyware.core.buffer :as buffer]
            [anyware.core.tree :as tree]
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

(defprotocol Node
  (render [node]))

(extend-protocol Node
  anyware.core.editor.Editor
  (render [node]
    (element :pre {:class "editor" :style (style @style/global)}
             (str (-> node (get-in keys/minibuffer) render)
                  \newline
                  (-> node (get-in keys/buffer) render))))
  anyware.core.buffer.Buffer
  (render [node] (-> node (tree/parse (-> node meta :parser)) render))
  anyware.core.tree.Label
  (render [{:keys [name value]}]
    (let [{:keys [foreground background]} (style/read name)]
      (element :span {:style (style {:color foreground
                                     :background-color background})}
               (render value))))
  java.lang.Character
  (render [node] (escape (str node)))
  java.lang.String
  (render [node] (escape node))
  clojure.lang.IPersistentVector
  (render [node] (reduce str (mapv render node))))
