(ns anyware.core.mode.insert
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer :as buffer]
            [anyware.core.mode.delete :as delete]))

(def left (lens/modify record/buffer buffer/left))

(def right (lens/modify record/buffer buffer/right))

(def up (lens/modify record/buffer buffer/up))

(def down (lens/modify record/buffer buffer/down))

(def keymap
  (atom {:backspace delete/backspace
         :enter (lens/modify record/buffer buffer/break)
         :left left
         :right right
         :up up
         :down down}))
