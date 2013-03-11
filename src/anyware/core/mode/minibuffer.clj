(ns anyware.core.mode.minibuffer
  (:require [anyware.core.buffer.character :as character]
            [anyware.core.lens :as lens]
            [anyware.core.command :as command]))

(def keymap
  (atom {:backspace (lens/modify :minibuffer character/backspace)
         :left (lens/modify :minibuffer character/prev)
         :right (lens/modify :minibuffer character/next)
         :enter command/run}))
