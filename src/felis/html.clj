(ns felis.html
  (:refer-clojure :exclude [< seq])
  (:require [clojure.string :as string]
            [felis.language :as language]
            [felis.buffer :as buffer]
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

(deftype EmptyNode []
  Node
  (render [_] ""))

(defn seq
  ([] (EmptyNode.))
  ([x & xs]
     (NodeSeq. x xs)))

(defn <
  ([label attributes content]
     (Element. label attributes content))
  ([label attributes content & contents]
     (Element. label attributes (NodeSeq. content contents))))

(defn pointer [{:keys [lefts rights]}]
  (list (< :span {:class "hidden"} lefts)
        (< :span {:class "pointer"} (-> rights (get 0 \space) str))
        "\n"))

(defn cursor [pointer]
  (apply < :span {:class "cursor"}
         pointer))

(defn html [editor]
  (let [{:keys [current minibuffer style]} (:root editor)
        {:keys [buffer language]} current
        {:keys [tops focus bottoms]} buffer]
    (< :html {}
       (< :head {}
          (< :title {} "felis")
          (< :style {:type "text/css"}
             (css style)))
       (< :body {}
          (< :div {:class "editor"}
             (< :pre {:class "buffer"}
                (->>  buffer
                      serialization/write
                      (language/highlight language))
                (cursor (concat (mapcat pointer tops)
                                (list (apply < :span {:class "focus"}
                                             (pointer focus)))
                                (mapcat pointer bottoms))))
             (< :div {:class "minibuffer"}
                (serialization/write minibuffer)
                (cursor (pointer minibuffer))))))))

(def style
  {:body {:margin "0px"}
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
   :.name {:color "blue"}
   :.special {:color "fuchsia"}
   :.string {:color "red"}
   :.keyword {:color "aqua"}
   :.comment {:color "maroon"}})
