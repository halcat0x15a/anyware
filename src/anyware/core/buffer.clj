(ns anyware.core.buffer
  (:refer-clojure :exclude [char empty read peek conj drop pop newline])
  (:require [clojure.string :as string]))

(defrecord Buffer [left right])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :right))

(defn write [{:keys [left right]}]
  (str left right))

(defmulti fold (fn [left right field] field))
(defmethod fold :right [_ right _] right)
(defmethod fold :left [left _ _] left)

(def inverse (partial fold :right :left))

(defmulti append (fn [field _ _] field))
(defmethod append :right [field value buffer]
  (assoc buffer field (str value (field buffer))))
(defmethod append :left [field value buffer]
  (assoc buffer field (str (field buffer) value)))

(defmulti substring (fn [field _ _] field))
(defmethod substring :right [field n buffer]
  (assoc buffer field (subs (field buffer) n)))
(defmethod substring :left [field n buffer]
  (assoc buffer
    field (subs (field buffer) 0 (-> buffer field count (- n)))))

(defn split [f regex field buffer]
  (if-let [result (->> buffer field (re-find (regex field)))]
    (->> buffer (substring field (count result)) (f result))
    buffer))

(defn delete
  ([regex field] (partial delete regex field)) 
  ([regex field buffer]
     (split (fn [_ buffer] buffer) regex field buffer)))

(defn move
  ([regex field] (partial move regex field))
  ([regex field buffer]
     (split (partial append (inverse field)) regex field buffer)))

(def char (partial fold #"[\s\S]$" #"^[\s\S]"))
(def line (partial fold #"\n??[^\n]*$" #"^[^\n]*\n??"))
(def word (partial fold #"\w+\W*$" #"^\W*\w+"))
(def buffer (constantly #"[\s\S]*"))

(defn command [buffer]
  (-> buffer write (string/split #"\s+")))

(defn cursor [field buffer]
  (-> buffer field count))

(defn lines [field buffer]
  (->> buffer field (filter (partial identical? \newline)) count))

(->> (read "foo\nbar\nbaz")
     (move line :right))
