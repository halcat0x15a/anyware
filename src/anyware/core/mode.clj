(ns anyware.core.mode
  (:require [clojure.zip :as zip]
            [clojure.walk :as walk]
            [anyware.core.lens :as lens]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]
            [anyware.core.command :as command]))

(declare normal)

(defn map-values [f keymap]
  (walk/walk (fn [[k v]] [k (f v)]) identity keymap))

(defn modify-values [lens keymap]
  (map-values (partial partial lens/modify lens) keymap))

(defn safe [f]
  (fn [x]
    (if-let [y (f x)] y x)))

(defn escape [editor]
  (lens/set :mode normal editor))

(defn input
  ([keymap] (input (fn [key editor] editor) keymap))
  ([f keymap]
     (fn [key]
       (fn [editor]
         (if-let [g ((merge keymap {:escape escape}) key)]
           (g editor)
           (f key editor))))))

(defn append [lens keymap]
  (input (fn [key editor]
           (lens/modify lens (partial buffer/append key) editor))
         keymap))

(def delete
  (->> {\h buffer/backspace
        \l buffer/delete
        \d buffer/delete}
       (modify-values lens/buffer)
       input))

(def insert
  (->> {:backspace buffer/backspace
        :enter buffer/break
        :left buffer/left
        :right buffer/right
        :up buffer/up
        :down buffer/down}
       (modify-values lens/buffer)
       (append lens/buffer)))

(def minibuffer
  (->> {:backspace buffer/backspace
        :left buffer/left
        :right buffer/right}
       (modify-values :minibuffer)
       (merge {:enter command/run})
       (append :minibuffer)))

(def buffer
  (modify-values
   lens/buffer
   {\h buffer/left
    \j buffer/down
    \k buffer/up
    \l buffer/right
    \0 buffer/head
    \9 buffer/tail
    \w buffer/forword
    \b buffer/backword
    \x (safe buffer/delete)
    \X (safe buffer/backspace)}))

(def history
  (->> {\u zip/up
        \r zip/down}
       (map-values safe)
       (modify-values lens/history)))

(def ->insert
  (->> {\a buffer/right
        \I buffer/head
        \A buffer/tail
        \o buffer/newline
        \O buffer/return
        \i identity}
       (modify-values lens/buffer)
       (map-values (partial comp (partial lens/set :mode insert)))))

(def mode
  (->> {\d delete
        \: minibuffer}
       (map-values (fn [f] (fn [editor] (assoc editor :mode f))))))

(def normal
  (input (merge buffer mode history ->insert)))
