(ns anyware.core.mode.delete
  (:require [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.buffer.character :as character]
            [anyware.core.buffer.line :as line]
            [anyware.core.buffer.word :as word]))

(def backspace (lens/modify record/buffer character/backspace))

(def delete (lens/modify record/buffer character/delete))

(def keymap
  (atom {\h backspace
         \l delete
         \$ (lens/modify record/buffer line/delete)
         \^ (lens/modify record/buffer line/backspace)
         \d (lens/modify record/buffer line/remove)
         \w (lens/modify record/buffer word/delete)
         \b (lens/modify record/buffer word/backspace)}))
