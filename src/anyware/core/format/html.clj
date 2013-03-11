(ns anyware.core.format.html
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.parser :as parser]
            [anyware.core.parser.ast :as ast]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]
            [anyware.core.format.color :as color]))

(def global
  (atom {:color "black"
         :background-color "white"
         :font-size "16px"
         :font-family "monospace"}))

(defn escape [string]
  (string/escape string {\< "&lt;"
                         \> "&gt;"
                         \& "&amp;"}))

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

(defprotocol Node
  (render [node]))

(extend-protocol Node
  anyware.core.editor.Editor
  (render [{:keys [minibuffer] :as editor}]
    (let [buffer (lens/get record/buffer editor)]
      (element :pre {:class "editor" :style (style @global)}
               (str (buffer/center (dec (get (meta editor) :height))
                                   (buffer/line :lefts buffer)
                                   (buffer/write buffer))
                    (buffer/write minibuffer)))))
  anyware.core.buffer.Buffer
  (render [{:keys [lefts] :as buffer}]
    (if-let [parser (-> buffer meta (get :parser))]
      (-> (->> buffer
               buffer/write
               parser
               ast/zip
               (ast/cursor (count lefts)))
          (zip/insert-left "<span>")
          (zip/insert-right "</span>"))
      (buffer/write buffer)))
  anyware.core.parser.ast.Node
  (render [{:keys [label value]}]
    (element :span
             {:style (style {:color (-> label color/read name)})}
             value)))
