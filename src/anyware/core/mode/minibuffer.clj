(ns anyware.core.mode.minibuffer
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.mode :as mode]
            [anyware.core.lens.record :as record]
            [anyware.core.command :as command]))

(def with (partial record/with :minibuffer))

(def keymap
  (atom {:backspace (with buffer/backspace)
         :left (with buffer/left)
         :right (with buffer/right)
         :enter command/run}))

(defmethod mode/keymap :minibuffer [_] @keymap)

(defmethod mode/default :minibuffer [_] (with buffer/append))
