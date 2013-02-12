(ns felis.html
  (:refer-clojure :exclude [<])
  (:require [clojure.string :as string]))

(defrecord Node [label attributes content])

(defn <
  ([label attributes content]
     (Node. label attributes content))
  ([label attributes content & contents]
     (Node. label attributes (vec (cons content contents)))))

(defn escape [string]
  (string/escape string {\< "&lt;" \> "&gt;" \& "&amp;"}))

(defn write
  ([label attributes content]
     (letfn [(attribute [attributes key value]
               (str attributes \space (name key) \= \" (name value) \"))]
       (let [label' (name label)]
         (str \< label' (reduce-kv attribute "" attributes) \>
              (write content)
              \< \/ label' \>))))
  ([{:keys [label attributes content] :as node}]
     (cond (string? node) (escape node)
           (vector? node) (->> node (mapv write) string/join)
           :else (write label attributes content))))

(defn css [style]
  (letfn [(declaration [declarations property value]
            (str declarations \space (name property) \: \space (name value) \;))
          (block [blocks selector block]
            (let [selector (name selector)
                  block (reduce-kv declaration "" block)]
              (str blocks \space selector \space \{ block \space \})))]
    (reduce-kv block "" style)))

(def style
  {:body {:margin :0px}
   :.editor {:color :black
             :background-color :white
             :font-size :16px
             :font-family :monospace}
   :.status {:color :black
             :background-color :silver
             :margin :0px}
   :.buffer {:position :relative
             :margin :0px}
   :.text {:position :absolute}
   :.cursor {:position :absolute
             :top :0px
             :left :0px}
   :.hidden {:visibility :hidden}
   :.pointer {:color :white
              :background-color :gray}
   :.focus {:color :white
            :background-color :black}
   :.minibuffer {:position :fixed
                 :bottom :0px
                 :margin :0px}
   :.name {:color :blue}
   :.special {:color :fuchsia}
   :.string {:color :red}
   :.keyword {:color :aqua}
   :.comment {:color :maroon}})
