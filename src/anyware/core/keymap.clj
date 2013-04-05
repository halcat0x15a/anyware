(ns anyware.core.keymap
  (:require [clojure.zip :as zip]
            [anyware.core.lens :refer (modify) :as lens]
            [anyware.core.record :refer (buffer history) :as record]
            [anyware.core.function :as function]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]
            [anyware.core.buffer.line :as line]
            [anyware.core.buffer.word :as word]))

(defmulti execute (fn [[f & args] editor] f))
(defmethod execute :default [_ editor] editor)

(defmulti normal identity)
(defmethod normal \l [_] (modify buffer character/next))
(defmethod normal \h [_] (modify buffer character/prev))
(defmethod normal \j [_] (modify buffer line/next))
(defmethod normal \k [_] (modify buffer line/prev))
(defmethod normal \w [_] (modify buffer word/next))
(defmethod normal \b [_] (modify buffer word/prev))
(defmethod normal \$ [_] (modify buffer line/end))
(defmethod normal \^ [_] (modify buffer line/begin))
(defmethod normal \x [_] (modify buffer character/delete))
(defmethod normal \X [_] (modify buffer character/backspace))
(defmethod normal #{:control \u} [_]
  (modify history (function/safe zip/up)))
(defmethod normal #{:control \r} [_]
  (modify history (function/safe zip/down)))
(defmethod normal \i [_] (lens/set :mode :insert))
(defmethod normal \a [_]
  (comp (lens/set :mode :insert) (modify buffer character/next)))
(defmethod normal \I [_]
  (comp (lens/set :mode :insert) (modify buffer line/begin)))
(defmethod normal \A [_]
  (comp (lens/set :mode :insert) (modify buffer line/end)))
(defmethod normal \o [_]
  (comp (lens/set :mode :insert) (modify buffer line/insert)))
(defmethod normal \O [_]
  (comp (lens/set :mode :insert) (modify buffer line/append)))
(defmethod normal \d [_] (lens/set :mode :delete))
(defmethod normal \: [_] (lens/set :mode :minibuffer))
(defmethod normal :default [_] identity)

(defmulti insert identity)
(defmethod insert :escape [_] (lens/set :mode :normal))
(defmethod insert :backspace [_] (modify buffer character/backspace))
(defmethod insert :enter [_] (modify buffer line/break))
(defmethod insert :right [_] (modify buffer character/next))
(defmethod insert :left [_] (modify buffer character/prev))
(defmethod insert :down [_] (modify buffer line/next))
(defmethod insert :up [_] (modify buffer line/prev))
(defmethod insert :default [char]
  (modify buffer (partial buffer/append char)))

(defmulti delete identity)
(defmethod delete :escape [_] (lens/set :mode :normal))
(defmethod delete \l [_] (modify buffer character/delete))
(defmethod delete \h [_] (modify buffer character/backspace))
(defmethod delete \$ [_] (modify buffer line/delete))
(defmethod delete \^ [_] (modify buffer line/backspace))
(defmethod delete \d [_] (modify buffer line/remove))
(defmethod delete \w [_] (modify buffer word/delete))
(defmethod delete \b [_] (modify buffer word/backspace))
(defmethod delete :default [_] identity)

(defmulti minibuffer identity)
(defmethod minibuffer :escape [_] (lens/set :mode :normal))
(defmethod minibuffer :backspace [_]
  (modify record/minibuffer character/backspace))
(defmethod minibuffer :right [_]
  (modify record/minibuffer character/next))
(defmethod minibuffer :left [_]
  (modify record/minibuffer character/prev))
(defmethod minibuffer :enter [_]
  (comp (lens/set record/minibuffer buffer/empty)
        #(execute (->> % (lens/get record/minibuffer) buffer/command) %)))
(defmethod minibuffer :default [char]
  (modify record/minibuffer (partial buffer/append char)))

(defmulti keymap identity)
(defmethod keymap :normal [_] normal)
(defmethod keymap :insert [_] insert)
(defmethod keymap :delete [_] delete)
(defmethod keymap :minibuffer [_] minibuffer)
(defmethod keymap :default [_] normal)
