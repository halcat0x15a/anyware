(ns anyware.editor
  (:require [anyware.buffer :as buffer]))

(declare normal-mode insert-mode)

(defrecord Editor [buffer mode])

(defn run [value editor]
  (let [mode (:mode editor)]
    (if-let [f (get mode value)]
      (f editor)
      ((get mode :default) value editor))))

(defn insert [value field editor]
  (if (or (char? value) (string? value))
    (assoc editor :buffer (buffer/insert value field (:buffer editor)))
    editor))

(defn delete [n field editor]
  (assoc editor :buffer (buffer/delete n field (:buffer editor))))

(defn move [n field editor]
  (assoc editor :buffer (buffer/move n field (:buffer editor))))

(defn move-left [editor]
  (move 1 :left editor))

(defn move-right [editor]
  (move 1 :right editor))

(defn set-mode [mode editor]
  (assoc editor :mode mode))

(defn html [editor]
  (buffer/text (:buffer editor)))

(def normal-mode
  {"i" #(set-mode insert-mode %)
   "h" move-left
   "l" move-right
   :left move-left
   :right move-right
   :default (fn [value editor] editor)})

(def insert-mode
  {:left move-left
   :right move-right
   :escape #(set-mode normal-mode %)
   :default (fn [value editor] (insert value :left editor))})

(def default (Editor. buffer/default normal-mode))
