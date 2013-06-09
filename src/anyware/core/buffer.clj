(ns anyware.core.buffer
  (:refer-clojure :exclude [empty read take])
  (:require [clojure.string :as string]))

(defprotocol Buffer
  (show [buffer]))

(defrecord Zipper [left right]
  Buffer
  (show [buffer] (str (string/reverse left) right)))

(def empty (Zipper. "" ""))

(def read (partial assoc empty :right))

(def inverse {:right :left, :left :right})

(defmulti normalize (fn [string field] field))
(defmethod normalize :left [string _] (string/reverse string))
(defmethod normalize :right [string _] string)

(defn insert [value field buffer]
  (update-in buffer [field] (partial str (normalize (str value) field))))

(defn field [f n default]
  (cond (pos? n) (f n :left)
        (neg? n) (f (- n) :right)
        :else default))

(defn substring
  ([n buffer] (field #(substring %1 %2 buffer) n buffer))
  ([n field buffer]
     (update-in buffer [field] #(subs % n))))

(defn take
  ([n buffer] (field #(take %1 %2 buffer) n ""))
  ([n field buffer]
     (-> buffer field (subs 0 n) (normalize field))))

(defn delete
  ([f field] (partial delete f field)) 
  ([f field buffer]
     (if-let [result (->> buffer field f)]
       (substring (count result) field buffer)
       buffer)))

(defn move
  ([f field] (partial move f field))
  ([f field buffer]
     (if-let [result (->> buffer field f)]
       (->> buffer
            (substring (count result) field)
            (insert result (inverse field)))
       buffer)))

(def character (comp str first))

(def line (partial re-find #"^[^\n]*\n??"))

(def word (partial re-find #"^\W*\w+"))

(defn cursor
  ([buffer] (cursor :left buffer))
  ([field buffer] (-> buffer field count)))

(def select #(vary-meta % assoc :mark (cursor :left %)))

(def deselect #(vary-meta % dissoc :mark))

(defn selection [f buffer]
  (if-let [mark (-> buffer meta :mark)]
    (f (- (cursor buffer) mark) buffer)))

(def copy (partial selection take))

(def cut (partial selection substring))

(defn command [buffer]
  (-> buffer show (string/split #"\s+")))
