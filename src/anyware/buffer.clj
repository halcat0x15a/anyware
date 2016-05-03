(ns anyware.buffer
  (:require [clojure.string :as string]
            [anyware.html :as html]))

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
    (let [cursor (first right)
          right (if cursor (subs right 1) "")
          cursor (condp = cursor
                   \newline (str \space \newline)
                   nil \space
                   cursor)]
      (html/elem "pre" {} (html/elem "code" {} [left (html/elem "span" {"class" "cursor"} cursor) right])))))

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
        length (count text)]
    (if (<= n length)
      (let [text (case field
                   :left (subs text 0 (- length n))
                   :right (subs text n))]
        (assoc buffer field text))
      buffer)))

(defn move [n field buffer]
  (let [text (get buffer field)
        length (count text)]
    (if (<= n length)
      (let [text (case field
                   :left (subs text (- length n))
                   :right (subs text 0 n))
            field' (case field
                     :left :right
                     :right :left)]
        (->> buffer (delete n field) (insert text field')))
      buffer)))

(defn move-line [field buffer]
  (let [text (get buffer field)]
    (if-let [i (case field
                 :left (string/last-index-of text \newline)
                 :right (string/index-of text \newline))]
      (move (case field :left (- (count text) i) :right (inc i)) field buffer)
      buffer)))
