(ns anyware.buffer
  (:require [clojure.string :as string]))

(defprotocol IBuffer
  (text [buffer])
  (row [buffer])
  (column [buffer])
  (html [buffer]))

(defrecord Buffer [left right]
  IBuffer
  (text [buffer] (str left right))
  (row [buffer]
    (reduce (fn [n c] (if (identical? c \newline) (inc n) n)) 0 left))
  (column [buffer]
    (let [n (count left)
          i (string/last-index-of left \newline)]
      (if (pos? i) (- n i) n)))
  (html [buffer]
    (let [right (condp = (first right)
                  \newline (str "<span class=\"cursor\"> </span>" \newline (subs right 1))
                  nil "<span class=\"cursor\"> </span>"
                  (str "<span class=\"cursor\">" (first right) "</span>" (subs right 1)))]
      (str "<pre><code>" left right "</code></pre>"))))

(defn create [text]
  (Buffer. "" text))

(def default (Buffer. "" ""))

(defn insert [value field buffer]
  (let [text (get buffer field)
        text (case field
                :left (str text value)
                :right (str value text))]
    (assoc buffer field text)))

(defn delete [n field buffer]
  (let [text (get buffer field)
        text (case field
                :left (subs text 0 (- (count text) n))
                :right (subs text n))]
    (assoc buffer field text)))

(defn move [n field buffer]
  (let [text (get buffer field)
        text (case field
               :left (subs text (- (count text) n))
               :right (subs text 0 n))
        field' (case field
                 :left :right
                 :right :left)]
    (->> buffer (delete n field) (insert text field'))))

(defn move-line [field buffer]
  (let [text (get buffer field)]
    (if-let [i (case field
                 :left (string/last-index-of text \newline)
                 :right (string/index-of text \newline))]
      (move (case field :left (- (count text) i) :right (inc i)) field buffer)
      buffer)))
