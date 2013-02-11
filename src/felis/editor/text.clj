(ns felis.editor.text
  (:refer-clojure :exclude [replace])
  (:require [felis.text :as text]
            [felis.node :as node]
            [felis.editor.edit :as edit]))

(defn left [editor]
  (update-in editor text/path edit/prev))

(defn right [editor]
  (update-in editor text/path edit/next))

(defn start [editor]
  (update-in editor text/path edit/start))

(defn end [editor]
  (update-in editor text/path edit/end))

(defn insert [editor char]
  (update-in editor text/path (partial edit/insert char)))

(defn append [editor char]
  (update-in editor text/path (partial edit/append char)))

(defn delete [editor]
  (update-in editor text/path edit/delete))

(defn backspace [editor]
  (update-in editor text/path edit/backspace))
