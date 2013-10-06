(ns anyware.core.buffer
  (:refer-clojure :exclude [empty complement])
  (:require [clojure.string :as string]))

(declare ->Viewer ->Editor)

(defprotocol Buffer
  (edit [buffer])
  (view [buffer])
  (show [buffer]))

(def complement {:left :right, :right :left})

(defmulti insert
  (fn [buffer key value]
    (if (string? buffer) [::str key] ::insert)))
(defmethod insert [::str :left] [buffer key value] (str buffer value))
(defmethod insert [::str :right] [buffer key value] (str value buffer))
(defmethod insert ::insert [buffer key value]
  (-> buffer edit (update-in [key] #(insert % key value))))

(defmulti delete
  (fn
    ([buffer n] ::drop)
    ([buffer key value]
       (cond (string? buffer) (if (>= (count buffer) (Math/abs value)) [::subs key] ::identity)
             (integer? value) ::drop'
             :else ::delete))))
(defmethod delete [::subs :left] [buffer key n]
  (subs buffer 0 (- (count buffer) n)))
(defmethod delete [::subs :right] [buffer key n]
  (subs buffer n))
(defmethod delete ::drop [buffer n]
  (delete buffer (if (pos? n) :right :left) (Math/abs n)))
(defmethod delete ::drop' [buffer key n]
  (-> buffer edit (update-in [key] #(delete % key n))))
(defmethod delete ::delete [buffer key regex]
  (->> buffer edit key (re-find (key regex)) count (delete buffer key)))
(defmethod delete ::identity [buffer key value] buffer)

(defn move
  ([buffer n]
     (let [viewer (view buffer)
           {:keys [cursor buffer]} viewer
           cursor (+ cursor n)]
       (if (<= 0 cursor (count buffer))
         (assoc viewer :cursor cursor)
         viewer)))
  ([buffer key regex]
     (let [editor (edit buffer)]
       (if-let [s (re-find (key regex) (key editor))]
         (-> editor
             (delete key (count s))
             (insert (complement key) s))
         buffer))))

(defrecord Editor [left right]
  Buffer
  (edit [this] this)
  (view [this]
    (with-meta (->Viewer (show this) (count left))
      (meta this)))
  (show [_] (str left right)))

(defrecord Viewer [buffer cursor]
  Buffer
  (edit [this]
    (with-meta (->Editor (subs buffer 0 cursor) (subs buffer cursor))
      (meta this)))
  (view [this] this)
  (show [_] buffer))

(def empty (Viewer. "" 0))

(def word {:left #"\w+\W*\z" :right #"\A\W*\w+"})

(def line {:left #"[^\n]*\z" :right #"\A[^\n]*"})

(defn select [buffer]
  (vary-meta buffer assoc :mark (-> buffer view :cursor)))

(defn deselect [buffer]
  (vary-meta buffer dissoc :mark))

(defn copy [buffer]
  (let [mark (-> buffer meta :mark)
        {:keys [buffer cursor]} (view buffer)]
    (apply subs buffer (sort [cursor mark]))))

(defn cut [buffer]
  (delete buffer (- (-> buffer view :cursor) (-> buffer meta :mark))))
