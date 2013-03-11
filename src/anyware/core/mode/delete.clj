(ns anyware.core.mode.delete
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer.character :as character]))

(def backspace (lens/modify record/buffer character/backspace))

(def delete (lens/modify record/buffer character/delete))

(def keymap
  (atom {\h backspace
         \l delete}))
