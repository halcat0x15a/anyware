(ns anyware.core.buffer
  (:refer-clojure :exclude [empty complement])
  (:require [clojure.string :as string]))

(defrecord Buffer [left right]
  Object
  (toString [this] (str left right)))

(defn buffer [string] (->Buffer "" string))

(def empty (buffer ""))

(defn mapping [left right] {:left left :right right})

(def complement (mapping :right :left))

(def character (mapping #"[\s\S]\z" #"\A[\s\S]"))

(def word (mapping #"\w+\W*\z" #"\A\W*\w+"))

(def line (mapping #"[^\n]*\z" #"\A[^\n]*"))

(defn string-insert [string key value]
  (case key
    :left (str string value)
    :right (str value string)))

(defn insert [buffer key value]
  (update-in buffer [key] string-insert key value))

(defn string-delete [string key n]
  (case key
    :left (subs string 0 (- (count string) n))
    :right (subs string n)))

(defn delete
  ([buffer n]
     (if (pos? n)
       (delete buffer :right n)
       (delete buffer :left (- n))))
  ([buffer key n]
     (update-in buffer [key] string-delete key n)))

(defn delete-matches [buffer key mapping]
  (delete buffer key (count (re-find (key mapping) (key buffer)))))

(defn move [buffer key mapping]
  (if-let [result (re-find (key mapping) (key buffer))]
    (-> buffer
        (delete key (count result))
        (insert (complement key) result))
    buffer))

(defn select [{:keys [left] :as buffer}]
  (vary-meta buffer assoc :mark (count left)))

(defn deselect [buffer]
  (vary-meta buffer dissoc :mark))

(defn copy [{:keys [left] :as buffer}]
  (if-let [mark (-> buffer meta :mark)]
    (apply subs (str buffer) (sort [(count left) mark]))))

(defn cut [{:keys [left] :as buffer}]
  (if-let [mark (-> buffer meta :mark)]
    (delete buffer (- (count left) mark))
    buffer))
