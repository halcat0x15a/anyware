(ns anyware.core.buffer
  (:refer-clojure :exclude [empty read peek conj drop pop newline])
  (:require [clojure.string :as string]))

(defn write [{:keys [lefts rights]}]
  (str lefts rights))

(defrecord Buffer [^String lefts ^String rights])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :rights))

(defmulti invert identity)
(defmethod invert :rights [_] :lefts)
(defmethod invert :lefts [_] :rights)

(defmulti peek (fn [field _] field))
(defmethod peek :rights [field ^Buffer buffer]
  (-> buffer field first))
(defmethod peek :lefts [field ^Buffer buffer]
  (-> buffer field last))

(defmulti conj (fn [field _ _] field))
(defmethod conj :rights [field value ^Buffer buffer]
  (update-in buffer [field] (partial str value)))
(defmethod conj :lefts [field value ^Buffer buffer]
  (update-in buffer [field] #(str % value)))

(defmulti drop (fn [_ field _] field))
(defmethod drop :rights [^long n field ^Buffer buffer]
  (update-in buffer [field] #(subs % n)))
(defmethod drop :lefts [^long n field ^Buffer buffer]
  (update-in buffer [field] #(subs % 0 (-> % count (- n)))))

(defn pop [field ^Buffer buffer]
  (if-not (-> buffer field empty?)
    (drop 1 field buffer)))

(defmulti extract (fn [field _] field))
(defmethod extract :rights [_ [_ _ rights]] rights)
(defmethod extract :lefts [_ [_ lefts _]] lefts)

(defn move [field ^Buffer buffer]
  (if-let [char (peek field buffer)]
    (->> buffer
         (conj (invert field) char)
         (pop field))
    buffer))

(def right (partial move :rights))

(def left (partial move :lefts))

(defn skip [regex field ^Buffer buffer]
  (if-let [result (->> buffer field (re-find regex))]
    (let [field' (invert field)]
      (->> (assoc buffer field (extract field result))
           (conj field' (extract field' result))))
    buffer))

(def down (partial skip #"^(.*\n)([\s\S]*)" :rights))

(def up (partial skip #"([\s\S]*)(\n.*)$" :lefts))

(def tail (partial skip #"^(.*)([\s\S]*)" :rights))

(def head (partial skip #"([\s\S]*?)(.*)$" :lefts))

(def forword (partial skip #"^(\s*\w+)([\s\S]*)" :rights))

(def backword (partial skip #"([\s\S]*?)(\w+\s*)$" :lefts))

(defn most [field ^Buffer buffer]
  (->> (assoc buffer field "")
       (conj (invert field) (field buffer))))

(def begin (partial most :lefts))

(def end (partial most :rights))

(def append (partial conj :lefts))

(def insert (partial conj :rights))

(def break (partial conj :lefts \newline))

(def backspace (partial pop :lefts))

(def delete (partial pop :rights))

(def newline (partial conj :lefts \newline))

(def return (partial conj :rights \newline))

(defn cursor [field ^Buffer buffer]
  (-> buffer field count))

(defn line [field ^Buffer buffer]
  (->> buffer field (filter (partial identical? \newline)) count))

(defn center [height line string]
  (let [n (- line (/ height 2))
        lines (string/split-lines string)]
    (string/join
     (take height
           (if (pos? n)
             (clojure.core/drop n lines)
             lines)))))
