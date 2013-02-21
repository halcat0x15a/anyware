(ns felis.editor.buffer
  (:require [felis.path :as path]
            [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.edit :as edit]))

(defn top [editor]
  (update-in editor path/buffer (partial edit/move :tops)))

(defn bottom [editor]
  (update-in editor path/buffer (partial edit/move :bottoms)))

(defn start [editor]
  (update-in editor path/buffer (partial edit/end :tops)))

(defn end [editor]
  (update-in editor path/buffer (partial edit/end :bottoms)))

(defn insert-newline [editor]
  (update-in editor path/buffer (partial edit/insert text/default :bottoms)))

(defn append-newline [editor]
  (update-in editor path/buffer (partial edit/insert text/default :tops)))

(defn break [editor]
  (update-in editor path/buffer buffer/break))

(defn delete [editor]
  (update-in editor path/buffer (partial edit/delete :bottoms)))

(defn backspace [editor]
  (update-in editor path/buffer (partial edit/delete :tops)))
