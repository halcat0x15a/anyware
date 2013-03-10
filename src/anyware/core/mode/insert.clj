(ns anyware.core.mode.insert
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.lens :as lens]
            [anyware.core.mode :as mode]))

(def keymap
  (atom {:backspace buffer/backspace
         :enter buffer/break
         :left buffer/left
         :right buffer/right
         :up buffer/up
         :down buffer/down}))

(defmethod mode/keymap :insert [_] @keymap)

(defmethod mode/default :insert [_]
  (fn [editor]
    (fn [key]
      (lens/modify (-> buffer/append meta :lens)
                   (partial buffer/append key)
                   editor))))
