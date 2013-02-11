(ns felis.editor.minibuffer
  (:refer-clojure :exclude [next])
  (:require [clojure.string :as string]
            [felis.key :as key]
            [felis.minibuffer :as minibuffer]
            [felis.text :as text]
            [felis.lisp.environment :as environment]
            [felis.editor :as editor]
            [felis.editor.edit :as edit]))

(defn run [editor]
  (let [[command & args]
        (-> editor
            (get-in minibuffer/text)
            text/write
            (string/split #" "))]
    (if-let [f (-> editor (get-in environment/path) (get (symbol command)))]
      (assoc-in (apply f editor args) minibuffer/text text/default)
      editor)))

(defn focus [editor]
  (update-in editor minibuffer/text text/focus))

(defn prev [editor]
  (update-in editor minibuffer/text edit/prev))

(defn next [editor]
  (update-in editor minibuffer/text edit/next))

(defn append [editor char]
  (update-in editor minibuffer/text (partial edit/append char)))

(defn backspace [editor]
  (update-in editor minibuffer/text edit/backspace))

(def keymap
  {key/enter run
   key/backspace backspace
   key/left prev
   key/right next})

(defrecord Minibuffer [root]
  editor/Editor
  (keymap [editor] keymap)
  (input [editor char] (append editor char)))
