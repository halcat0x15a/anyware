(ns felis.html
  (:refer-clojure :exclude [< seq])
  (:require [clojure.string :as string]
            [felis.parser :as parser]
            [felis.buffer :as buffer]))

(def style
  (atom {:body {:margin "0px"}
         :.editor {:color "black"
                   :background-color "white"
                   :font-size "16px"
                   :font-family "monospace"}
         :.buffer {:position "relative"
                   :margin "0px"}
         ".buffer .cursor" {:position "absolute"
                            :top "0px"
                            :left "0px"}
         :.minibuffer {:position "fixed"
                       :bottom "0px"
                       :margin "0px"}
         ".minibuffer .cursor" {:position "fixed"
                                :bottom "0px"
                                :left "0px"}
         :.hidden {:visibility "hidden"}
         :.pointer {:color "white"
                    :background-color "gray"}
         ".focus .pointer" {:color "white"
                            :background-color "black"}
         :.symbol {:color "blue"}
         :.special {:color "fuchsia"}
         :.string {:color "red"}
         :.keyword {:color "aqua"}
         :.comment {:color "maroon"}
         :.list {:background-color "rgba(255, 0, 0, 0.1)"}
         :.vector {:background-color "rgba(0, 255, 0, 0.1)"}
         :.map {:background-color "rgba(0, 0, 255, 0.1)"}}))

(defprotocol Node
  (render [node]))

(defn escape [string]
  (string/escape string {\< "&lt;"
                         \> "&gt;"
                         \& "&amp;"
                         \space "&#160;"}))

(defn write [node]
  (cond (string? node) (escape node)
        :else (render node)))

(defn- attribute [attributes key value]
  (str attributes \space (name key) \= \" value \"))

(deftype Element [label attributes content]
  Node
  (render [_]
    (let [label (name label)]
      (str \< label (reduce-kv attribute "" attributes) \>
           (write content)
           \< \/ label \>))))

(deftype NodeSeq [first next]
  Node
  (render [_]
    (->> next
         (map write)
         (reduce str (write first)))))

(deftype EmptyNode []
  Node
  (render [_] ""))

(deftype Escape [string]
  Node
  (render [_] string))

(defn seq
  ([] (EmptyNode.))
  ([x & xs]
     (NodeSeq. x xs)))

(defn <
  ([label attributes content]
     (Element. label attributes content))
  ([label attributes content & contents]
     (Element. label attributes (NodeSeq. content contents))))

(defn- declaration [declarations property value]
  (str declarations \space (name property) \: \space value \;))

(defn- rule [rules selector block]
  (let [selector (name selector)
        block (reduce-kv declaration "" block)]
    (str rules \space selector \space \{ block \space \})))

(defn css [style]
  (->> style (reduce-kv rule "") Escape.))

(defn pointer [{:keys [lefts rights]}]
  (list (< :span {:class "hidden"} lefts)
        (< :span {:class "pointer"} (-> rights (get 0 \space) str))
        "\n"))

(defn cursor [pointer]
  (apply < :span {:class "cursor"}
         pointer))

(defn html [editor]
  (let [{:keys [current minibuffer]} (:root editor)
        {:keys [buffer language]} current]
    (< :html {}
       (< :head {}
          (< :title {} "felis")
          (< :style {:type "text/css"}
             (css @style)))
       (< :body {}
          (< :div {:class "editor"}
             (< :pre {:class "buffer"}
                (buffer/write buffer))
             (< :div {:class "minibuffer"}
                (buffer/write minibuffer)))))))
