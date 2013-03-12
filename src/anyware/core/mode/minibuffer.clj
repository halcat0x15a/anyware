(ns anyware.core.mode.minibuffer
  (:require [anyware.core.buffer.character :as character]
            [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.command :as command]))

(def keymap
  (atom {:backspace (lens/modify record/minibuffer character/backspace)
         :left (lens/modify record/minibuffer character/prev)
         :right (lens/modify record/minibuffer character/next)
         :enter command/run}))
