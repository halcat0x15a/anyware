(ns felis.editor.text
  (:refer-clojure :exclude [replace])
  (:require [felis.path :as path]
            [felis.edit :as edit]
            [felis.text :as text]))

(defn left [editor]
  (update-in editor path/focus text/left))

(defn right [editor]
  (update-in editor path/focus text/right))

(defn start [editor]
  (update-in editor path/focus (partial edit/end :lefts)))

(defn end [editor]
  (update-in editor path/focus (partial edit/end :rights)))

(defn append [editor char]
  (update-in editor path/focus (partial text/append char)))

(defn delete [editor]
  (update-in editor path/focus (partial edit/delete :rights)))

(defn backspace [editor]
  (update-in editor path/focus text/backspace))
