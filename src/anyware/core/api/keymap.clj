(ns anyware.core.api.keymap
  (:refer-clojure :exclude [char])
  (:require [anyware.core.path
             :refer (mode clipboard buffer history)
             :as path]
            [anyware.core.history :as history]
            [anyware.core.buffer
             :refer (move char line word)
             :as buffer]
            [anyware.core.api.command :as command]))

(defn dispatch [key editor] key)

(defmulti normal dispatch)
(defmulti insert dispatch)
(defmulti delete dispatch)
(defmulti minibuffer dispatch)

(defmethod normal :escape [_ editor]
  (update-in editor buffer buffer/deselect))
(defmethod normal \l [_ editor]
  (update-in editor buffer (move char :right)))
(defmethod normal \h [_ editor]
  (update-in editor buffer (move char :left)))
(defmethod normal \j [_ editor]
  (->> editor (normal \l) (normal \$)))
(defmethod normal \k [_ editor]
  (->> editor (normal \h) (normal \^)))
(defmethod normal \w [_ editor]
  (update-in editor buffer (move word :right)))
(defmethod normal \b [_ editor]
  (update-in editor buffer (move word :left)))
(defmethod normal \$ [_ editor]
  (update-in editor buffer (move line :right)))
(defmethod normal \^ [_ editor]
  (update-in editor buffer (move line :left)))
(defmethod normal \v [_ editor]
  (update-in editor buffer buffer/select))
(defmethod normal \y [_ editor]
  (if-let [string
           (buffer/selection (get-in editor buffer))]
    (update-in editor clipboard (history/commit string) editor)
    editor))
(defmethod normal \x [_ editor] (delete \h editor))
(defmethod normal \X [_ editor] (delete \l editor))
(defmethod normal #{:control \u} [_ editor]
  (update-in editor history history/undo))
(defmethod normal #{:control \r} [_ editor]
  (update-in editor history history/redo))
(defmethod normal \i [_ editor] (assoc-in editor mode insert))
(defmethod normal \I [_ editor] (->> editor (normal \i) (normal \^)))
(defmethod normal \a [_ editor] (->> editor (normal \i) (normal \l)))
(defmethod normal \A [_ editor] (->> editor (normal \i) (normal \$)))
(defmethod normal \o [_ editor]
  (->> editor (normal \i) (insert :enter) (normal \$)))
(defmethod normal \O [_ editor]
  (->> editor (normal \i) (normal \h) (insert :enter) (normal \^)))
(defmethod normal \d [_ editor] (assoc-in editor mode delete))
(defmethod normal \: [_ editor] (assoc-in editor mode minibuffer))
(defmethod normal :default [_ editor] editor)

(defmethod insert :escape [key editor]
  (-> editor
      (assoc-in mode normal)
      (update-in history history/commit)))
(defmethod insert :backspace [_ editor] (normal \x editor))
(defmethod insert :enter [_ editor] (insert \newline editor))
(defmethod insert :right [_ editor] (normal \l editor))
(defmethod insert :left [_ editor] (normal \h editor))
(defmethod insert :down [_ editor] (normal \j editor))
(defmethod insert :up [_ editor] (normal \k))
(defmethod insert :default [char editor]
  (update-in editor buffer (partial buffer/append :left char)))

(defmethod delete :escape [key] (insert key))
(defmethod delete \l [_ editor]
  (update-in editor buffer (buffer/delete char :right)))
(defmethod delete \h [_ editor]
  (update-in editor buffer (buffer/delete char :left)))
(defmethod delete \$ [_ editor]
  (update-in editor buffer (buffer/delete line :right)))
(defmethod delete \^ [_ editor]
  (update-in editor buffer (buffer/delete line :left)))
(defmethod delete \d [_ editor]
  (->> editor (delete \$) (delete \^) (delete \h)))
(defmethod delete \w [_ editor]
  (update-in editor buffer (buffer/delete word :right)))
(defmethod delete \b [_ editor]
  (update-in editor buffer (buffer/delete word :left)))
(defmethod delete :default [_ editor] identity)

(defmethod minibuffer :escape [_ editor]
  (->> (assoc-in editor mode normal)
       (get-in history/commit path/minibuffer)
       (update-in editor history)))
(defmethod minibuffer :backspace [_ editor]
  (update-in editor path/minibuffer (buffer/delete char :left)))
(defmethod minibuffer :right [key editor]
  (update-in editor path/minibuffer (move char key)))
(defmethod minibuffer :left [key editor]
  (update-in editor path/minibuffer (move char key)))
(defmethod minibuffer :enter [_ editor]
  (assoc-in (->> (get-in editor path/minibuffer)
                 buffer/command
                 (apply command/exec editor))
            path/minibuffer buffer/empty))

(defmethod minibuffer :default [key editor]
  (update-in editor path/minibuffer (partial buffer/append :left key)))
