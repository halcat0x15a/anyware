(ns anyware.core.api
  (:refer-clojure :exclude [char])
  (:require [anyware.core.frame :as frame]
            [anyware.core.history :as history]
            [anyware.core.buffer
             :refer [move char line word]
             :as buffer]
            [anyware.core.language :as language]))

(def frame [:frame])

(def mode [:mode])

(def clipboard [:clipboard])

(def history (conj frame 0))

(def change (conj history 0))

(def buffer (conj change :current))

(def command [:command])

(def minibuffer (-> command (conj 0) (conj :current)))

(def contents (conj clipboard 0))

(def paths [frame mode clipboard history change buffer command minibuffer])

(defn validate [editor] (every? (partial get-in editor) paths))

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

(defn open [editor name string]
  (let [buffer (vary-meta (-> string buffer/read history/create)
                 assoc :parser (language/extension name))]
    (update-in editor frame (frame/update name buffer))))

(defn new [editor name] (open editor name ""))

(def commands
  (atom {"next" next-buffer
         "prev" prev-buffer
         "new" new}))

(defn execute [editor]
  (let [[f & args] (-> editor (get-in minibuffer) buffer/command)]
    (if-let [f (@commands f)]
      (-> (apply f editor args)
          (update-in command history/commit)
          (assoc-in minibuffer buffer/empty))
      editor)))

(defn run [{:keys [mode] :as editor} key]
  (if-let [f (get mode key)]
    (f editor)
    ((get mode :default) editor key)))
