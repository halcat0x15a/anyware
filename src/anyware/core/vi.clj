(ns anyware.core.vi
  (:refer-clojure :exclude [char])
  (:require [anyware.core.api :as api]
            [anyware.core.path :refer [mode] :as path]
            [anyware.core.buffer :refer [move char] :as buffer]))

(declare normal insert delete minibuffer)

(def normal-mode #(assoc-in % mode normal))
(def insert-mode #(assoc-in % mode insert))
(def delete-mode #(assoc-in % mode delete))
(def minibuffer-mode #(assoc-in % mode minibuffer))

(def normal
  {:escape api/deselect
   \l api/right
   \h api/left
   \j api/down
   \k api/up
   \w api/right-word
   \b api/left-word
   \$ api/end-of-line
   \^ api/beginning-of-line
   \v api/select
   \y api/copy
   \x api/delete
   \X api/backspace
   #{:control \u} api/undo
   #{:control \r} api/redo
   \i insert-mode
   \I (comp insert-mode api/beginning-of-line)
   \a (comp insert-mode api/right)
   \A (comp insert-mode api/end-of-line)
   \o (comp insert-mode api/break api/end-of-line)
   \O (comp insert-mode api/left api/break api/beginning-of-line)
   \d delete-mode
   \: minibuffer-mode
   :default identity})

(def insert
  {:escape (comp normal-mode api/commit)
   :backspace api/backspace
   :enter api/break
   :right api/right
   :left api/left
   :down api/down
   :up api/up
   :default api/insert})

(def delete
  {:escape (comp normal-mode api/commit)
   \l api/delete
   \h api/backspace
   \$ api/delete-right
   \^ api/delete-left
   \d api/delete-line
   \w api/delete-right-word
   \b api/delete-left-word
   :default identity})

(def minibuffer
  {:escape normal-mode
   :backspace #(update-in % path/minibuffer (buffer/delete char :left))
   :right #(update-in % path/minibuffer (move char :right))
   :left #(update-in % path/minibuffer (move char :left))
   :enter api/execute-command
   :default #(update-in %1 %2 (partial buffer/append :left key))})
