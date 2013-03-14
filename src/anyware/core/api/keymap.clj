(ns anyware.core.api.keymap
  (:require [anyware.core.api :as api]))

(defmulti normal identity)
(defmethod normal \l [_] api/forward-character)
(defmethod normal \h [_] api/backward-character)
(defmethod normal \j [_] api/forward-line)
(defmethod normal \k [_] api/backward-line)
(defmethod normal \w [_] api/forward-word)
(defmethod normal \b [_] api/backward-word)
(defmethod normal \$ [_] api/end-of-line)
(defmethod normal \^ [_] api/beginning-of-line)
(defmethod normal \x [_] api/delete-forward-character)
(defmethod normal \X [_] api/delete-backward-character)
(defmethod normal #{:control \u} [_] api/undo-buffer)
(defmethod normal #{:control \r} [_] api/redo-buffer)
(defmethod normal \i [_] api/insert-mode)
(defmethod normal \a [_]
  (comp api/insert-mode api/forward-character))
(defmethod normal \I [_]
  (comp api/insert-mode api/beginning-of-line))
(defmethod normal \A [_]
  (comp api/insert-mode api/end-of-line))
(defmethod normal \o [_]
  (comp api/insert-mode api/insert-newline-into-forward))
(defmethod normal \O [_]
  (comp api/insert-mode api/insert-newline-into-backward))
(defmethod normal \d [_] api/delete-mode)
(defmethod normal \: [_] api/minibuffer-mode)
(defmethod normal :default [_] identity)

(defmulti insert identity)
(defmethod insert :escape [_] api/normal-mode)
(defmethod insert :backspace [_] api/delete-backward-character)
(defmethod insert :enter [_] api/break-line)
(defmethod insert :right [_] api/forward-character)
(defmethod insert :left [_] api/backward-character)
(defmethod insert :down [_] api/forward-line)
(defmethod insert :up [_] api/backward-line)
(defmethod insert :default [char]
  (partial api/insert-string char))

(defmulti delete identity)
(defmethod delete :escape [_] api/normal-mode)
(defmethod delete \l [_] api/delete-forward-character)
(defmethod delete \h [_] api/delete-backward-character)
(defmethod delete \$ [_] api/delete-forward-line)
(defmethod delete \^ [_] api/delete-backward-line)
(defmethod delete \d [_] api/delete-line)
(defmethod delete \w [_] api/delete-forward-word)
(defmethod delete \b [_] api/delete-backward-word)
(defmethod delete :default [_] identity)

(defmulti minibuffer identity)
(defmethod minibuffer :escape [_] api/normal-mode)
(defmethod minibuffer :right [_] api/forward-minibuffer-character)
(defmethod minibuffer :left [_] api/backward-minibuffer-character)
(defmethod minibuffer :default [char]
  (partial api/insert-string-into-minibuffer char))

(defmulti keymap identity)
(defmethod keymap :normal [_] normal)
(defmethod keymap :insert [_] insert)
(defmethod keymap :delete [_] delete)
(defmethod keymap :minibuffer [_] minibuffer)
(defmethod keymap :default [_] normal)
