(ns anyware.core.api
  (:require [clojure.zip :as zip]
            [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.function :as function]
            [anyware.core.command :as command]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.character :as character]
            [anyware.core.buffer.line :as line]
            [anyware.core.buffer.word :as word]))

(def forward-character (lens/modify record/buffer character/next))
(def backward-character (lens/modify record/buffer character/prev))
(def forward-line (lens/modify record/buffer line/next))
(def backward-line (lens/modify record/buffer line/prev))
(def forward-word (lens/modify record/buffer word/next))
(def backward-word (lens/modify record/buffer word/prev))
(def end-of-line (lens/modify record/buffer line/end))
(def beginning-of-line (lens/modify record/buffer line/begin))

(defn insert-string [string editor]
  (lens/modify record/buffer (partial buffer/append string) editor))
(def break-line (lens/modify record/buffer line/break))
(def insert-newline-into-backward
  (lens/modify record/buffer line/insert))
(def insert-newline-into-forward
 (lens/modify record/buffer line/append))

(def delete-forward-character
  (lens/modify record/buffer character/delete))
(def delete-backward-character
  (lens/modify record/buffer character/backspace))
(def delete-forward-line (lens/modify record/buffer line/delete))
(def delete-backward-line (lens/modify record/buffer line/backspace))
(def delete-line (lens/modify record/buffer line/remove))
(def delete-forward-word (lens/modify record/buffer word/delete))
(def delete-backward-word (lens/modify record/buffer word/backspace))

(def undo-buffer (lens/modify record/history (function/safe zip/up)))
(def redo-buffer (lens/modify record/history (function/safe zip/down)))

(def forward-minibuffer-character
  (lens/modify record/minibuffer character/next))
(def backward-minibuffer-character
  (lens/modify record/minibuffer character/prev))
(defn insert-string-into-minibuffer [string editor]
  (lens/modify record/minibuffer (partial buffer/append string) editor))

(def normal-mode (lens/set :mode :normal))
(def insert-mode (lens/set :mode :insert))
(def delete-mode (lens/set :mode :delete))
(def minibuffer-mode (lens/set :mode :minibuffer))

(defn execute-command [editor]
  (->> editor
       (command/exec (->> editor
                          (lens/get record/minibuffer)
                          buffer/command))
       (lens/set record/minibuffer buffer/empty)))
