(ns felis.editor.buffer
  (:require [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.editor.edit :as edit]))

(defn top [editor]
  (update-in editor buffer/path edit/prev))

(defn bottom [editor]
  (update-in editor buffer/path edit/next))

(defn start [editor]
  (update-in editor buffer/path edit/start))

(defn end [editor]
  (update-in editor buffer/path edit/end))

(defn insert-newline [editor]
  (update-in editor buffer/path (partial edit/insert text/default)))

(defn break [editor]
  (update-in editor buffer/path (partial buffer/break :rights)))

(defn append-newline [editor]
  (update-in editor buffer/path (partial edit/append text/default)))

(defn delete [editor]
  (update-in editor buffer/path edit/delete))

(defn backspace [editor]
  (update-in editor buffer/path edit/backspace))
