(ns anyware.core.keymap
  (:refer-clojure :exclude [char])
  (:require [clojure.zip :as zip]
            [anyware.core.lens :refer (modify) :as lens]
            [anyware.core.record :refer (buffer history) :as record]
            [anyware.core.function :as function]
            [anyware.core.buffer
             :refer (move char line word)
             :as buffer]
            [anyware.core.editor :as editor]))

(defmulti normal identity)
(defmulti insert identity)
(defmulti delete identity)
(defmulti minibuffer identity)

(defmethod normal \l [_] (modify buffer (move char :right)))
(defmethod normal \h [_] (modify buffer (move char :left)))
(defmethod normal \j [_] (comp (normal \l) (normal \$)))
(defmethod normal \k [_] (comp (normal \h) (normal \^)))
(defmethod normal \w [_] (modify buffer (move word :right)))
(defmethod normal \b [_] (modify buffer (move word :left)))
(defmethod normal \$ [_] (modify buffer (move line :right)))
(defmethod normal \^ [_] (modify buffer (move line :left)))
(defmethod normal \x [_] (delete \h))
(defmethod normal \X [_] (delete \l))
(defmethod normal #{:control \u} [_]
  (modify history (function/safe zip/up)))
(defmethod normal #{:control \r} [_]
  (modify history (function/safe zip/down)))
(defmethod normal \i [_] (lens/set :mode :insert))
(defmethod normal \I [_] (comp (normal \i) (normal \^)))
(defmethod normal \a [_] (comp (normal \i) (normal \l)))
(defmethod normal \A [_] (comp (normal \i) (normal \$)))
(defmethod normal \o [_] (comp (normal \i) (insert :enter) (normal \$)))
(defmethod normal \O [_]
  (comp (normal \i) (normal \h) (insert :enter) (normal \^)))
(defmethod normal \d [_] (lens/set :mode :delete))
(defmethod normal \: [_] (lens/set :mode :minibuffer))
(defmethod normal :default [_] identity)

(defmethod insert :escape [key] (minibuffer key))
(defmethod insert :backspace [_] (normal \x))
(defmethod insert :enter [_] (insert \newline))
(defmethod insert :right [_] (normal \l))
(defmethod insert :left [_] (normal \h))
(defmethod insert :down [_] (normal \j))
(defmethod insert :up [_] (normal \k))
(defmethod insert :default [char]
  (modify buffer (partial buffer/append :left char)))

(defmethod delete :escape [key] (minibuffer key))
(defmethod delete \l [_] (modify buffer (buffer/delete char :right)))
(defmethod delete \h [_] (modify buffer (buffer/delete char :left)))
(defmethod delete \$ [_] (modify buffer (buffer/delete line :right)))
(defmethod delete \^ [_] (modify buffer (buffer/delete line :left)))
(defmethod delete \d [_] (comp (delete \$) (delete \^) (delete \h)))
(defmethod delete \w [_] (modify buffer (buffer/delete word :right)))
(defmethod delete \b [_] (modify buffer (buffer/delete word :left)))
(defmethod delete :default [_] identity)

(defmethod minibuffer :escape [_] (lens/set :mode :normal))
(defmethod minibuffer :backspace [_]
  (modify record/minibuffer (buffer/delete char :left)))
(defmethod minibuffer :right [key]
  (modify record/minibuffer (move char key)))
(defmethod minibuffer :left [key]
  (modify record/minibuffer (move char key)))
(defmethod minibuffer :enter [_]
  (comp (lens/set record/minibuffer buffer/empty) editor/exec))
(defmethod minibuffer :default [key]
  (modify record/minibuffer (partial buffer/append :left key)))

(defmulti keymap identity)
(defmethod keymap :normal [_] normal)
(defmethod keymap :insert [_] insert)
(defmethod keymap :delete [_] delete)
(defmethod keymap :minibuffer [_] minibuffer)
(defmethod keymap :default [_] normal)
