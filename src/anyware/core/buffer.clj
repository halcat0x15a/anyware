(ns anyware.core.buffer
  (:refer-clojure :exclude [empty read peek conj drop pop newline])
  (:require [clojure.string :as string]))

(defn write [{:keys [lefts rights]}]
  (str lefts rights))

(defrecord Buffer [lefts rights])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :rights))

(defmulti invert identity)
(defmethod invert :rights [_] :lefts)
(defmethod invert :lefts [_] :rights)

(defmulti peek (fn [field _] field))
(defmethod peek :rights [field buffer]
  (-> buffer field first))
(defmethod peek :lefts [field buffer]
  (-> buffer field last))

(defmulti conj (fn [field _ _] field))
(defmethod conj :rights [field value buffer]
  (update-in buffer [field] (partial str value)))
(defmethod conj :lefts [field value buffer]
  (update-in buffer [field] #(str % value)))

(defmulti drop (fn [_ field _] field))
(defmethod drop :rights [n field buffer]
  (update-in buffer [field] #(subs % n)))
(defmethod drop :lefts [n field buffer]
  (update-in buffer [field] #(subs % 0 (-> % count (- n)))))

(defn pop [field buffer]
  (if-not (-> buffer field empty?)
    (drop 1 field buffer)))

(defn move
  ([field] (partial move field))
  ([field buffer]
     (->> (assoc buffer field "")
          (conj (invert field) (field buffer)))))

(def begin (move :lefts))

(def end (move :rights))

(defn command [buffer]
  (-> buffer write (string/split #"\s+")))

(defn cursor [field buffer]
  (-> buffer field count))

(defn line [field buffer]
  (->> buffer field (filter (partial identical? \newline)) count))

(defn center [height line string]
  (let [n (- line (/ height 2))
        lines (string/split-lines string)]
    (string/join
     (take height
           (if (pos? n)
             (clojure.core/drop n lines)
             lines)))))
