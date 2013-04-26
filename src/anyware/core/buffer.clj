(ns anyware.core.buffer
  (:refer-clojure :exclude [char empty read find complement])
  (:require [clojure.string :as string]))

(defrecord Buffer [left right])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :right))

(defn write [{:keys [left right]}] (str left right))

(defn pair [left right] {:left left :right right})

(def complement (pair :right :left))

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

(defn delete
  ([f field] (partial delete f field)) 
  ([f field buffer]
     (if-let [result (->> buffer field ((f field)))]
       (substring field (count result) buffer)
       buffer)))

(defn move
  ([f field] (partial move f field))
  ([f field buffer]
     (if-let [result (->> buffer field ((f field)))]
       (->> buffer
            (substring field (count result))
            (append (complement field) result))
       buffer)))

(def char (pair (comp str last) (comp str first)))

(def line
  (pair (partial re-find #"\n??[^\n]*\z")
        (partial re-find #"\A[^\n]*\n??")))

(def word
  (pair (partial re-find #"\w+\W*\z")
        (partial re-find #"\A\W*\w+")))

(def buffer (constantly identity))

(defn cursor [field buffer] (-> buffer field count))

(defn select [buffer]
  (vary-meta buffer assoc :mark (cursor :left buffer)))

(defn deselect [buffer]
  (vary-meta buffer dissoc :mark))

(defn selection [buffer]
  (if-let [mark (-> buffer meta :mark)]
    (let [n (- (cursor buffer) mark)]
      (cond (pos? n) (->> buffer :left (take-last n) string/join)
            (neg? n) (subs (:right buffer) 0 n)
            :else ""))))

(defn command [buffer]
  (-> buffer write (string/split #"\s+")))
