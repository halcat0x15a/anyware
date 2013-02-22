(ns felis.editor.delete
  (:require [felis.key :as key]
            [felis.editor :as editor]
            [felis.editor.history :as history]
            [felis.buffer :as buffer]
            [felis.editor.text :as text]))

(def keymap
  {key/escape history/commit
   key/left text/left
   key/up buffer/top
   key/down buffer/bottom
   key/right text/right
   \h text/backspace
   \j buffer/delete
   \k buffer/backspace
   \l text/delete
   \d buffer/delete})

(defrecord Delete [root]
  editor/Editor
  (keymap [editor] keymap)
  (input [editor char] editor))
