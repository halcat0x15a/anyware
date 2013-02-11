(ns felis.editor.buffer
  (:require [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.node :as node]
            [felis.editor.edit :as edit]))

(def top
  (partial buffer/update edit/prev))

(def bottom
  (partial buffer/update edit/next))

(def start
  (partial buffer/update edit/start))

(def end
  (partial buffer/update edit/end))

(def insert-newline
  (partial buffer/update (partial edit/insert text/default)))

(defn break [editor]
  (update-in editor buffer/path (partial buffer/break :rights)))

(def append-newline
  (partial buffer/update (partial edit/append text/default)))

(def delete
  (partial buffer/update edit/delete))

(def backspace
  (partial buffer/update edit/backspace))
