(ns anyware.core.mode.minibuffer
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.lens :as lens]
            [anyware.core.command :as command]))

(def keymap
  (atom {:backspace (lens/modify :minibuffer buffer/backspace)
         :left (lens/modify :minibuffer buffer/left)
         :right (lens/modify :minibuffer buffer/right)
         :enter command/run}))
