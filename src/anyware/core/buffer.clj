(ns anyware.core.buffer
  (:refer-clojure :exclude [empty read take])
  (:require [clojure.string :as string]))

(defrecord Buffer [left right])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :right))

(defn write [{:keys [left right]}] (str (string/reverse left) right))

(def inverse {:right :left, :left :right})

(defmulti normalize (fn [string field] field))
(defmethod normalize :left [string _] (string/reverse string))
(defmethod normalize :right [string _] string)

(defprotocol Text
  (insert [text field buffer]))

(extend-protocol Text
  java.lang.Character
  (insert [char field buffer]
    (assoc buffer
      field (str char (field buffer))))
  java.lang.String
  (insert [string field buffer]
    (assoc buffer
      field (str (normalize string field) (field buffer)))))

(defn substring [n field buffer]
  (update-in buffer [field] #(subs % n)))

(defn take [n field buffer]
  (-> buffer field (subs 0 n) (normalize field)))

(defn delete
  ([f field] (partial delete f field)) 
  ([f field buffer]
     (if-let [result (->> buffer field f)]
       (substring (count result) field buffer)
       buffer)))

(defn move
  ([f field] (partial move f field))
  ([f field buffer]
     (if-let [result (->> buffer field f str)]
       (->> buffer
            (substring (count result) field)
            (insert result (inverse field)))
       buffer)))

(def line (partial re-find #"^[^\n]*\n??"))

(def word (partial re-find #"^\W*\w+"))

(defn cursor
  ([buffer] (cursor :left buffer))
  ([field buffer] (-> buffer field count)))

(def select #(vary-meta % assoc :mark (cursor :left %)))

(def deselect #(vary-meta % dissoc :mark))

(defn field [n]
  (cond (pos? n) :left
        (neg? n) :right))

(defn selection [f buffer]
  (if-let [mark (-> buffer meta :mark)]
    (let [n (- (cursor buffer) mark)
          n' (if (neg? n) (- n) n)]
      (if-let [field (field n)]
        (f n' field buffer)))))

(def copy (partial selection take))

(def cut (partial selection substring))

(defn command [buffer]
  (-> buffer write (string/split #"\s+")))
