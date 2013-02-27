(ns anyware.core.html
  (:refer-clojure :exclude [< seq])
  (:require [clojure.string :as string]
            [clojure.zip :as zip]
            [anyware.core.parser.ast :as ast]
            [anyware.core.buffer :as buffer]))

(def style
  (atom {"body" {:margin "0px"}
         ".editor" {:color "black"
                   :background-color "white"
                   :font-size "16px"
                   :font-family "monospace"}
         ".buffer" {:position "relative"
                   :margin "0px"}
         ".buffer .cursor" {:position "absolute"
                            :top "0px"
                            :left "0px"}
         ".minibuffer" {:position "fixed"
                       :bottom "0px"
                       :margin "0px"}
         ".minibuffer .cursor" {:position "fixed"
                                :bottom "0px"
                                :left "0px"}
         ".hidden" {:visibility "hidden"}
         ".pointer" {:color "white"
                    :background-color "gray"}
         ".focus .pointer" {:color "white"
                            :background-color "black"}
         ".symbol" {:color "blue"}
         ".special" {:color "fuchsia"}
         ".string" {:color "red"}
         ".keyword" {:color "aqua"}
         ".comment" {:color "maroon"}
         ".list" {:background-color "rgba(255, 0, 0, 0.1)"}
         ".vector" {:background-color "rgba(0, 255, 0, 0.1)"}
         ".map" {:background-color "rgba(0, 0, 255, 0.1)"}}))

(defprotocol Node
  (render [node]))

(deftype Escape [string]
  Node
  (render [_] string))

(defn- declaration [declarations property value]
  (str declarations (name property) \: \space value \; \space))

(defn- rule [rules selector block]
  (str rules \space selector \space \{
       \space (reduce-kv declaration "" block)
       \}))

(defn css [style]
  (->> style (reduce-kv rule "") Escape.))

(defn escape [string]
  (string/escape string {\< "&lt;"
                         \> "&gt;"
                         \& "&amp;"}))

(defn write [node]
  (cond (string? node) (escape node)
        (vector? node) (reduce str "" (map write node))
        :else (render node)))

(deftype Element [label attributes content]
  Node
  (render [_]
    (letfn [(attribute [attributes key value]
              (str attributes \space (name key) \= \" value \"))]
      (let [label (name label)]
        (str \< label (reduce-kv attribute "" attributes) \>
             (write content)
             \< \/ label \>)))))

(defn <
  ([label attributes content]
     (Element. label attributes content))
  ([label attributes content & contents]
     (Element. label attributes (apply vector content contents))))

(extend-protocol Node
  anyware.core.buffer.Buffer
  (render [{:keys [lefts rights parser] :as buffer}]
    (let [string (buffer/write buffer)]
      (write [string
              (get (parser string) :result "")
              (< :span {:class "cursor"}
                 (< :span {:class "hidden"} lefts)
                 (< :span {:class "pointer"}
                    (-> rights (get 0 \space) str)))])))
  anyware.core.parser.ast.Node
  (render [{:keys [label value]}]
    (write (< :span {:class (name label)} value))))

(defn html [{:keys [history minibuffer]}]
  (< :html {}
     (< :head {}
        (< :title {} "Anyware")
        (< :style {:type "text/css"} (css @style)))
     (< :body {}
        (< :div {:class "editor"}
           (< :pre {:class "buffer"} (zip/node history))
           (< :div {:class "minibuffer"} minibuffer)))))
