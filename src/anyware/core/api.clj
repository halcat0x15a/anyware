(ns anyware.core.api
  (:require [clojure.zip :as zip]
            [anyware.core.lens :refer (modify) :as lens]
            [anyware.core.record :refer (buffer minibuffer history)]
            [anyware.core.function :as function]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]
            [anyware.core.buffer.line :as line]
            [anyware.core.buffer.word :as word]))

(def forward-character (modify buffer character/next))
(def backward-character (modify buffer character/prev))
(def forward-line (modify buffer line/next))
(def backward-line (modify buffer line/prev))
(def forward-word (modify buffer word/next))
(def backward-word (modify buffer word/prev))
(def end-of-line (modify buffer line/end))
(def beginning-of-line (modify buffer line/begin))

(defn insert-string [string editor]
  (modify buffer (partial buffer/append string) editor))
(def break-line (modify buffer line/break))
(def insert-newline-into-backward
  (modify buffer line/insert))
(def insert-newline-into-forward
 (modify buffer line/append))

(def delete-forward-character
  (modify buffer character/delete))
(def delete-backward-character
  (modify buffer character/backspace))
(def delete-forward-line (modify buffer line/delete))
(def delete-backward-line (modify buffer line/backspace))
(def delete-line (modify buffer line/remove))
(def delete-forward-word (modify buffer word/delete))
(def delete-backward-word (modify buffer word/backspace))

(def undo-buffer (modify history (function/safe zip/up)))
(def redo-buffer (modify history (function/safe zip/down)))

(def forward-minibuffer-character
  (modify minibuffer character/next))
(def backward-minibuffer-character
  (modify minibuffer character/prev))
(defn insert-string-into-minibuffer [string editor]
  (modify minibuffer (partial buffer/append string) editor))
(def clear-minibuffer (lens/set minibuffer buffer/empty))

(def normal-mode (lens/set :mode :normal))
(def insert-mode (lens/set :mode :insert))
(def delete-mode (lens/set :mode :delete))
(def minibuffer-mode (lens/set :mode :minibuffer))

(defmulti execute (fn [[f & args] editor] f))
(defmethod execute :default [_ editor] editor)

(defn execute-command [editor]
  (execute (->> editor (lens/get minibuffer) buffer/command)))
