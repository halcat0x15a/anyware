(ns anyware.core.mode.insert
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer.character :as character]
            [anyware.core.buffer.line :as line]
            [anyware.core.mode.delete :as delete]))

(def left (lens/modify record/buffer character/prev))

(def right (lens/modify record/buffer character/next))

(def up (lens/modify record/buffer line/prev))

(def down (lens/modify record/buffer line/next))

(def keymap
  (atom {:backspace delete/backspace
         :enter (lens/modify record/buffer line/break)
         :left left
         :right right
         :up up
         :down down}))
