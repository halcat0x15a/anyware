(ns anyware.core.mode.normal
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]
            [anyware.core.mode :as mode]
            [anyware.core.mode.insert :as insert]
            [anyware.core.mode.delete :as delete]
            [anyware.core.mode.minibuffer :as minibuffer]))

(def insert (lens/set :mode :insert))

(def head (lens/modify record/buffer buffer/head))

(def tail (lens/modify record/buffer buffer/tail))

(def keymap
  (atom {\h insert/left
         \j insert/down
         \k insert/up
         \l insert/right
         \0 head
         \9 tail
         \w (lens/modify record/buffer buffer/forword)
         \b (lens/modify record/buffer buffer/backword)
         \x (lens/modify record/buffer buffer/delete)
         \X (lens/modify record/buffer buffer/backspace)
         \u (lens/modify record/history history/undo)
         \r (lens/modify record/history history/redo)
         \a (comp insert insert/right)
         \I (comp insert head)
         \A (comp insert tail)
         \o (comp insert (lens/modify record/buffer buffer/newline))
         \O (comp insert (lens/modify record/buffer buffer/return))
         \i insert
         \d (lens/set :mode :delete)
         \: (lens/set :mode :minibuffer)}))

(defmethod mode/keymap :normal [_] @keymap)
