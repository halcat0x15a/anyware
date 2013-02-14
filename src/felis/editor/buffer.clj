(ns felis.editor.buffer
  (:require [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.edit :as edit]))

(defn top [editor]
  (update-in editor buffer/path (partial edit/move :tops)))

(defn bottom [editor]
  (update-in editor buffer/path (partial edit/move :bottoms)))

(defn start [editor]
  (update-in editor buffer/path (partial edit/end :tops)))

(defn end [editor]
  (update-in editor buffer/path (partial edit/end :bottoms)))

(defn insert-newline [editor]
  (update-in editor buffer/path (partial edit/insert text/default :tops)))

(defn append-newline [editor]
  (update-in editor buffer/path (partial edit/insert text/default :bottoms)))

(defn break [editor]
  (update-in editor buffer/path buffer/break))

(defn delete [editor]
  (update-in editor buffer/path (partial edit/delete :tops)))

(defn backspace [editor]
  (update-in editor buffer/path (partial edit/delete :bottoms)))
