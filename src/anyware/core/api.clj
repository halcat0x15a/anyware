(ns anyware.core.api
  (:refer-clojure :exclude [char])
  (:require [anyware.core.path
             :refer [mode clipboard buffer history frame contents]
             :as path]
            [anyware.core.frame :as frame]
            [anyware.core.history :as history]
            [anyware.core.buffer
             :refer [move char line word]
             :as buffer]))

(def right #(update-in % buffer (move char :right)))

(def left #(update-in % buffer (move char :left)))

(def right-word #(update-in % buffer (move word :right)))

(def left-word #(update-in % buffer (move word :left)))

(def end-of-line #(update-in % buffer (move line :right)))

(def beginning-of-line #(update-in % buffer (move line :left)))

(def down (comp right end-of-line))

(def up (comp left beginning-of-line))

(def select #(update-in % buffer buffer/select))

(def deselect #(update-in % buffer buffer/deselect))

(defn copy [editor]
  (if-let [string (buffer/selection (get-in editor buffer))]
    (update-in editor clipboard (history/commit string) editor)
    editor))

(defn insert [editor char]
  (update-in editor buffer (partial buffer/append :left char)))

(def break #(insert % \newline))

(defn paste [editor] (insert editor (get-in editor contents)))

(def backspace #(update-in % buffer (buffer/delete char :left)))

(def delete #(update-in % buffer (buffer/delete char :right)))

(def delete-right #(update-in % buffer (buffer/delete line :right)))

(def delete-left #(update-in % buffer (buffer/delete line :left)))

(def delete-line (comp backspace delete-right delete-left))

(def delete-right-word
  #(update-in % buffer (buffer/delete word :right)))

(def delete-left-word
  #(update-in % buffer (buffer/delete word :left)))

(def undo #(update-in % history history/undo))

(def redo #(update-in % history history/redo))

(def commit #(update-in % history history/commit))

(def next-buffer #(update-in % frame frame/next))

(def prev-buffer #(update-in % frame frame/prev))

(defn new-buffer [editor name]
  (update-in editor frame (frame/update name (history/create ""))))

(def command
  {"next" next-buffer
   "prev" prev-buffer
   "new" new-buffer})

(defn execute-command [editor]
  (let [[f & args] (-> editor (get-in path/minibuffer) buffer/command)]
    (if-let [f (command f)]
      (-> (apply f editor args)
          (update-in path/command history/commit)
          (assoc-in path/minibuffer buffer/empty))
      editor)))

(defn run [editor key]
  (if-let [f (get-in editor (conj mode key))]
    (f editor)
    ((get-in editor (conj mode :default)) editor key)))
