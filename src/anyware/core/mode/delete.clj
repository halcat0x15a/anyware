(ns anyware.core.mode.delete
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer :as buffer]))

(def backspace (lens/modify record/buffer buffer/backspace))

(def delete (lens/modify record/buffer buffer/delete))

(def keymap
  (atom {\h backspace
         \l delete}))
