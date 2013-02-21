(ns felis.editor.text
  (:refer-clojure :exclude [replace])
  (:require [felis.path :as path]
            [felis.edit :as edit]))

(defn left [editor]
  (update-in editor path/focus edit/left))

(defn right [editor]
  (update-in editor path/focus edit/right))

(defn start [editor]
  (update-in editor path/focus (partial edit/end :lefts)))

(defn end [editor]
  (update-in editor path/focus (partial edit/end :rights)))

(defn append [editor char]
  (update-in editor path/focus (partial edit/append char)))

(defn delete [editor]
  (update-in editor path/focus (partial edit/delete :rights)))

(defn backspace [editor]
  (update-in editor path/focus edit/backspace))
