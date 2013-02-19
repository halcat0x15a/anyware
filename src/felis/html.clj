(ns felis.html
  (:refer-clojure :exclude [<])
  (:require [clojure.string :as string]
            [felis.serialization :as serialization]))

(defprotocol Node
  (render [node]))

(defn escape [string]
  (string/escape string {\< "&lt;" \> "&gt;" \& "&amp;"}))

(defn write [node]
  (cond (string? node) (escape node)
        :else (render node)))

(defn- declaration [declarations property value]
  (str declarations \space (name property) \: \space value \;))

(defn- rule [rules selector block]
  (let [selector (name selector)
        block (reduce-kv declaration "" block)]
    (str rules \space selector \space \{ block \space \})))

(def css (partial reduce-kv rule ""))

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

(defn <
  ([label attributes content]
     (Element. label attributes content))
  ([label attributes content & contents]
     (Element. label attributes (NodeSeq. content contents))))

(defn html [editor]
  (let [{:keys [workspace minibuffer style]} (:root editor)
        {:keys [buffer]} workspace]
    (< :html {}
       (< :head {}
          (< :title {} "felis")
          (< :style {:type "text/css"}
             (css style)))
       (< :body {}
          (< :div {:class "buffer"}
             (serialization/write buffer))
          (< :div {:class "minibuffer"}
             (serialization/write minibuffer))))))

(def style
  {:body {:margin "0px"}
   :.editor {:color "black"
             :background-color "white"
             :font-size "16px"
             :font-family "monospace"}
   :.status {:color "black"
             :background-color "silver"
             :margin "0px"}
   :.buffer {:position "relative"
             :margin "0px"}
   :.text {:position "absolute"}
   :.cursor {:position "absolute"
             :top "0px"
             :left "0px"}
   :.hidden {:visibility "hidden"}
   :.pointer {:color "white"
              :background-color "gray"}
   :.focus {:color "white"
            :background-color "black"}
   :.minibuffer {:position "fixed"
                 :bottom "0px"
                 :margin "0px"}
   :.name {:color "blue"}
   :.special {:color "fuchsia"}
   :.string {:color "red"}
   :.keyword {:color "aqua"}
   :.comment {:color "maroon"}})
