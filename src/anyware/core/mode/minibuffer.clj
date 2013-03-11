(ns anyware.core.mode.minibuffer
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.mode :as mode]
            [anyware.core.lens :as lens]
            [anyware.core.command :as command]))

(def keymap
  (atom {:backspace (lens/modify :minibuffer buffer/backspace)
         :left (lens/modify :minibuffer buffer/left)
         :right (lens/modify :minibuffer buffer/right)
         :enter command/run}))

(defmethod mode/keymap :minibuffer [_] @keymap)

(defmethod mode/default :minibuffer [_ key editor]
  (lens/modify :minibuffer (partial buffer/append key) editor))
