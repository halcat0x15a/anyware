(ns anyware.core.buffer
  (:refer-clojure :exclude [empty read peek conj drop pop newline])
  (:require [clojure.string :as string]))

(defn write [{:keys [left right]}]
  (str left right))

(defrecord Buffer [left right])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :right))

(defmulti inverse identity)
(defmethod inverse :right [_] :left)
(defmethod inverse :left [_] :right)

(defmulti peek (fn [field _] field))
(defmethod peek :right [field buffer]
  (-> buffer field first))
(defmethod peek :left [field buffer]
  (-> buffer field last))

(defmulti conj (fn [field _ _] field))
(defmethod conj :right [field value buffer]
  (update-in buffer [field] (partial str value)))
(defmethod conj :left [field value buffer]
  (update-in buffer [field] #(str % value)))

(defmulti drop (fn [_ field _] field))
(defmethod drop :right [n field buffer]
  (update-in buffer [field] #(subs % n)))
(defmethod drop :left [n field buffer]
  (update-in buffer [field] #(subs % 0 (-> % count (- n)))))

(defn move
  ([field] (partial move field))
  ([field buffer]
     (->> (assoc buffer field "")
          (conj (inverse field) (field buffer)))))

(def begin (move :left))

(def end (move :right))

(def append (partial conj :left))

(defn command [buffer]
  (-> buffer write (string/split #"\s+")))

(defn cursor [field buffer]
  (-> buffer field count))

(defn line [field buffer]
  (->> buffer field (filter (partial identical? \newline)) count))
