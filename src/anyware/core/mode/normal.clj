(ns anyware.core.mode.normal
  (:require [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.buffer.line :as line]
            [anyware.core.buffer.word :as word]
            [anyware.core.history :as history]
            [anyware.core.mode.insert :as insert]
            [anyware.core.mode.delete :as delete]))

(def insert (lens/set :mode :insert))

(def head (lens/modify record/buffer line/begin))

(def tail (lens/modify record/buffer line/end))

(def keymap
  (atom {\h insert/left
         \j insert/down
         \k insert/up
         \l insert/right
         \0 head
         \9 tail
         \w (lens/modify record/buffer word/next)
         \b (lens/modify record/buffer word/prev)
         \x delete/delete
         \X delete/backspace
         \u (lens/modify record/history history/undo)
         \r (lens/modify record/history history/redo)
         \a (comp insert insert/right)
         \I (comp insert head)
         \A (comp insert tail)
         \o (comp insert (lens/modify record/buffer line/append))
         \O (comp insert (lens/modify record/buffer line/insert))
         \i insert
         \d (lens/set :mode :delete)
         \: (lens/set :mode :minibuffer)}))
