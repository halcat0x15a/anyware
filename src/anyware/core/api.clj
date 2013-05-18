(ns anyware.core.api
  (:refer-clojure :exclude [char])
  (:require [anyware.core.frame :as frame]
            [anyware.core.history :as history]
            [anyware.core.keys :refer :all]
            [anyware.core.buffer
             :refer [move char line word]
             :as buffer]
            [anyware.core.language :as language]))

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
  (if-let [string (buffer/copy (get-in editor buffer))]
    (update-in editor clipboard (history/commit string) editor)
    editor))

(defn cut [editor]
  (-> editor
      copy
      (update-in buffer buffer/cut)))

(defn insert
  ([editor in] (insert buffer editor in))
  ([path] (partial insert path))
  ([path editor in]
     (if (or (set? in) (keyword? in))
       editor
       (update-in editor path (partial buffer/append :left in)))))

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

(defn notice [editor message]
  (assoc-in editor minibuffer (buffer/read message)))

(defn open
  ([editor name] (open editor name ""))
  ([editor name string]
     (let [buffer (vary-meta (-> string buffer/read history/create)
                             assoc :parser (language/extension name))]
       (update-in editor frame (frame/update name buffer)))))

(def commands
  (atom {"next" next-buffer
         "prev" prev-buffer
         "new" open}))

(defn execute
  ([editor]
     (execute (-> editor (get-in minibuffer) buffer/command) editor))
  ([[f & args] editor]
     (if-let [f (@commands f)]
       (-> (apply f editor args)
           (update-in command history/commit)
           (assoc-in minibuffer buffer/empty))
       editor)))

(defn run [{:keys [mode] :as editor} key]
  (if-let [f (get mode key)]
    (f editor)
    ((get mode :default) editor key)))
