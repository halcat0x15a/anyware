(ns anyware.core.format.html
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.parser :as parser]
            [anyware.core.parser.ast :as ast]
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
         (escape content)
         \< \/ label \>)))

(defn- declaration [declarations property value]
  (str declarations (name property) \: value \;))

(def style (partial reduce-kv declaration ""))

(extend-protocol format/Node
  anyware.core.editor.Editor
  (render [editor]
    (element :pre {:class "editor" :style (style @global)}
             (str (->> editor (lens/get record/buffer) format/render)
                  (->> editor (lens/get record/minibuffer) format/render))))
  anyware.core.buffer.Buffer
  (render [buffer]
    (if-let [parser (-> buffer meta (get :parser))]
      (ast/parse parser buffer)
      (buffer/write buffer)))
  anyware.core.parser.ast.Node
  (render [{:keys [label value]}]
    (element :span
             {:style (style {:color (-> label color/read name)})}
             value)))
