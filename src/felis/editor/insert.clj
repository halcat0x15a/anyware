(ns felis.editor.insert
  (:require [felis.key :as key]
            [felis.editor :as editor]
            [felis.editor.history :as history]
            [felis.editor.buffer :as buffer]
            [felis.editor.text :as text]))

(def keymap
  {key/escape history/commit
   key/left text/left
   key/right text/right
   key/up buffer/top
   key/down buffer/bottom
   key/backspace text/backspace
   key/enter buffer/break})

(defrecord Insert [root]
  editor/Editor
  (keymap [editor] keymap)
  (input [editor char]
    (text/append editor char)))
