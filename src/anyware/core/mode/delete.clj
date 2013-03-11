(ns anyware.core.mode.delete
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer :as buffer]
            [anyware.core.mode :as mode]))

(def keymap
  (atom {\h (lens/modify record/buffer buffer/backspace)
         \l (lens/modify record/buffer buffer/delete)}))

(defmethod mode/keymap :delete [_] @keymap)
