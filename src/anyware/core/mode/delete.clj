(ns anyware.core.mode.delete
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.mode :as mode]))

(def keymap
  (atom {\h buffer/backspace
         \l buffer/delete
         \d buffer/delete}))

(defmethod mode/keymap :delete [_] @keymap)
