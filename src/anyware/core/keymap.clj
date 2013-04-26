(ns anyware.core.keymap
  (:refer-clojure :exclude [char])
  (:require [anyware.core.function :refer (combine)]
            [anyware.core.record
             :refer (modify buffer history)
             :as record]
            [anyware.core.history :as history]
            [anyware.core.buffer
             :refer (move char line word)
             :as buffer]
            [anyware.core.command :as command]
            [anyware.core.editor :as editor]))

(defmulti normal identity)
(defmulti insert identity)
(defmulti delete identity)
(defmulti minibuffer identity)

(defmethod normal :escape [_] (modify buffer buffer/deselect))
(defmethod normal \l [_] (modify buffer (move char :right)))
(defmethod normal \h [_] (modify buffer (move char :left)))
(defmethod normal \j [_] (comp (normal \l) (normal \$)))
(defmethod normal \k [_] (comp (normal \h) (normal \^)))
(defmethod normal \w [_] (modify buffer (move word :right)))
(defmethod normal \b [_] (modify buffer (move word :left)))
(defmethod normal \$ [_] (modify buffer (move line :right)))
(defmethod normal \^ [_] (modify buffer (move line :left)))
(defmethod normal \v [_] (modify buffer buffer/select))
(defmethod normal \y [_]
  (fn [editor]
    (if-let [string
             (buffer/selection (record/get buffer editor))]
      (modify :clipboard (history/commit string) editor)
      editor)))
(defmethod normal \x [_] (delete \h))
(defmethod normal \X [_] (delete \l))
(defmethod normal #{:control \u} [_] (modify history history/undo))
(defmethod normal #{:control \r} [_] (modify history history/redo))
(defmethod normal \i [_] (record/set :mode :insert))
(defmethod normal \I [_] (comp (normal \i) (normal \^)))
(defmethod normal \a [_] (comp (normal \i) (normal \l)))
(defmethod normal \A [_] (comp (normal \i) (normal \$)))
(defmethod normal \o [_] (comp (normal \i) (insert :enter) (normal \$)))
(defmethod normal \O [_]
  (comp (normal \i) (normal \h) (insert :enter) (normal \^)))
(defmethod normal \d [_] (record/set :mode :delete))
(defmethod normal \: [_] (record/set :mode :minibuffer))
(defmethod normal :default [_] identity)

(defmethod insert :escape [key]
  (comp (record/set :mode :normal) (modify history history/commit)))
(defmethod insert :backspace [_] (normal \x))
(defmethod insert :enter [_] (insert \newline))
(defmethod insert :right [_] (normal \l))
(defmethod insert :left [_] (normal \h))
(defmethod insert :down [_] (normal \j))
(defmethod insert :up [_] (normal \k))
(defmethod insert :default [char]
  (modify buffer (partial buffer/append :left char)))

(defmethod delete :escape [key] (insert key))
(defmethod delete \l [_] (modify buffer (buffer/delete char :right)))
(defmethod delete \h [_] (modify buffer (buffer/delete char :left)))
(defmethod delete \$ [_] (modify buffer (buffer/delete line :right)))
(defmethod delete \^ [_] (modify buffer (buffer/delete line :left)))
(defmethod delete \d [_] (comp (delete \$) (delete \^) (delete \h)))
(defmethod delete \w [_] (modify buffer (buffer/delete word :right)))
(defmethod delete \b [_] (modify buffer (buffer/delete word :left)))
(defmethod delete :default [_] identity)

(defmethod minibuffer :escape [_]
  (combine (comp history/commit buffer/write (record/get record/minibuffer))
           (record/set :mode :normal)
           (modify history)))
(defmethod minibuffer :backspace [_]
  (modify record/minibuffer (buffer/delete char :left)))
(defmethod minibuffer :right [key]
  (modify record/minibuffer (move char key)))
(defmethod minibuffer :left [key]
  (modify record/minibuffer (move char key)))
(defmethod minibuffer :enter [_]
  (fn [editor]
    (record/set record/minibuffer
                buffer/empty
                ((->> editor
                      (record/get record/minibuffer)
                      buffer/command
                      (apply command/exec))
                 editor))))
(defmethod minibuffer :default [key]
  (modify record/minibuffer (partial buffer/append :left key)))

(def keymap
  (atom {:normal normal
         :insert insert
         :delete delete
         :minibuffer minibuffer}))
