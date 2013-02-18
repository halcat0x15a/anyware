(ns felis.editor.text
  (:refer-clojure :exclude [replace])
  (:require [felis.text :as text]
            [felis.edit :as edit]))

(defn left [editor]
  (update-in editor text/path edit/left))

(defn right [editor]
  (update-in editor text/path edit/right))

(defn start [editor]
  (update-in editor text/path (partial edit/end :lefts)))

(defn end [editor]
  (update-in editor text/path (partial edit/end :rights)))

(defn append [editor char]
  (update-in editor text/path (partial edit/append char)))

(defn delete [editor]
  (update-in editor text/path (partial edit/delete :rights)))

(defn backspace [editor]
  (update-in editor text/path edit/backspace))
