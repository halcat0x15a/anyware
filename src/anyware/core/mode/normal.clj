(ns anyware.core.mode.normal
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]
            [anyware.core.mode :as mode]
            [anyware.core.mode.insert :as insert]
            [anyware.core.mode.delete :as delete]
            [anyware.core.mode.minibuffer :as minibuffer]))

(def insert (mode/set :insert))

(def keymap
  (atom {\h buffer/left
         \j buffer/down
         \k buffer/up
         \l buffer/right
         \0 buffer/head
         \9 buffer/tail
         \w buffer/forword
         \b buffer/backword
         \x buffer/delete
         \X buffer/backspace
         \u history/undo
         \r history/redo
         \a (record/comp insert buffer/right)
         \I (record/comp insert buffer/head)
         \A (record/comp insert buffer/tail)
         \o (record/comp insert buffer/newline)
         \O (record/comp insert buffer/return)
         \i insert
         \d (mode/set :delete)
         \: (mode/set :minibuffer)}))

(defmethod mode/keymap :normal [_] @keymap)
