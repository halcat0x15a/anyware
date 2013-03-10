(ns anyware.core.buffer
  (:refer-clojure :exclude [empty read peek conj drop pop newline])
  (:require [clojure.string :as string]
            [anyware.core.lens.record :as record]))

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

(def right (record/with record/buffer (partial move :rights)))

(def left (record/with record/buffer (partial move :lefts)))

(defn skip [regex field ^Buffer buffer]
  (if-let [result (->> buffer field (re-find regex))]
    (let [field' (invert field)]
      (->> (assoc buffer field (extract field result))
           (conj field' (extract field' result))))
    buffer))

(def down
  (record/with record/buffer (partial skip #"^(.*\n)([\s\S]*)" :rights)))

(def up
  (record/with record/buffer (partial skip #"([\s\S]*)(\n.*)$" :lefts)))

(def tail
  (record/with record/buffer (partial skip #"^(.*)([\s\S]*)" :rights)))

(def head
  (record/with record/buffer (partial skip #"([\s\S]*?)(.*)$" :lefts)))

(def forword
  (record/with record/buffer (partial skip #"^(\s*\w+)([\s\S]*)" :rights)))

(def backword
  (record/with record/buffer (partial skip #"([\s\S]*?)(\w+\s*)$" :lefts)))

(defn most [field ^Buffer buffer]
  (->> (assoc buffer field "")
       (conj (invert field) (field buffer))))

(def begin (record/with record/buffer (partial most :lefts)))

(def end (record/with record/buffer (partial most :rights)))

(def append (record/with record/buffer (partial conj :lefts)))

(def insert (record/with record/buffer (partial conj :rights)))

(def break (record/with record/buffer (partial conj :lefts \newline)))

(def backspace
  (record/with record/buffer (partial (record/safe pop) :lefts)))

(def delete
  (record/with record/buffer (partial (record/safe pop) :rights)))

(def newline (record/with record/buffer (partial conj :lefts \newline)))

(def return (record/with record/buffer (partial conj :rights \newline)))

(defn cursor [field ^Buffer buffer]
  (-> buffer field count))

(defn line [field ^Buffer buffer]
  (->> buffer field (filter (partial identical? \newline)) count))

(defn center [^long height ^long line ^String string]
  (let [n (- line (/ height 2))
        lines (string/split-lines string)]
    (string/join
     (take height
           (if (pos? n)
             (clojure.core/drop n lines)
             lines)))))
