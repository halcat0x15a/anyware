(ns felis.editor.minibuffer
  (:refer-clojure :exclude [next])
  (:require [clojure.string :as string]
            [felis.key :as key]
            [felis.serialization :as serialization]
            [felis.lisp.environment :as environment]
            [felis.text :as text]
            [felis.editor :as editor]
            [felis.edit :as edit]))

(defn run [editor]
  (let [[command & args]
        (-> editor
            (get-in text/minibuffer)
            serialization/write
            (string/split #" "))]
    (if-let [f (-> editor (get-in environment/path) (get (symbol command)))]
      (assoc-in (apply f editor args) text/minibuffer text/default)
      editor)))

(defn left [editor]
  (update-in editor text/minibuffer edit/left))

(defn right [editor]
  (update-in editor text/minibuffer edit/right))

(defn append [editor char]
  (update-in editor text/minibuffer (partial edit/append char)))

(defn backspace [editor]
  (update-in editor text/minibuffer edit/backspace))

(def keymap
  {key/enter run
   key/backspace backspace
   key/left left
   key/right right})

(defrecord Minibuffer [root]
  editor/Editor
  (keymap [editor] keymap)
  (input [editor char] (append editor char)))