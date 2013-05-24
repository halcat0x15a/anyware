(ns anyware.core.api
  (:refer-clojure :exclude [char])
  (:require [anyware.core.frame :as frame]
            [anyware.core.history :as history]
            [anyware.core.keys :as keys]
            [anyware.core.buffer :as buffer]
            [anyware.core.language :as language]))

(defn right
  ([editor] (right keys/buffer editor))
  ([key editor]
     (update-in editor key (buffer/move buffer/char :right))))

(defn left
  ([editor] (left keys/buffer editor))
  ([key editor]
     (update-in editor key (buffer/move buffer/char :left))))

(def right-word
  #(update-in % keys/buffer (buffer/move buffer/word :right)))

(def left-word
  #(update-in % keys/buffer (buffer/move buffer/word :left)))

(def end-of-line
  #(update-in % keys/buffer (buffer/move buffer/line :right)))

(def beginning-of-line
  #(update-in % keys/buffer (buffer/move buffer/line :left)))

(def down (comp right end-of-line))

(def up (comp left beginning-of-line))

(def select #(update-in % keys/buffer buffer/select))

(def deselect #(update-in % keys/buffer buffer/deselect))

(defn copy [editor]
  (if-let [string (buffer/copy (get-in editor keys/buffer))]
    (update-in editor keys/clipboard (history/commit string) editor)
    editor))

(defn cut [editor]
  (-> editor
      copy
      (update-in keys/buffer buffer/cut)))

(defn insert
  ([editor in] (insert keys/buffer editor in))
  ([key] (partial insert key))
  ([key editor in]
     (if (or (set? in) (keyword? in))
       editor
       (update-in editor key (partial buffer/append :left in)))))

(def break #(insert % \newline))

(defn paste [editor] (insert editor (get-in editor keys/clip)))

(defn backspace
  ([editor]
     (backspace keys/buffer editor))
  ([key editor]
     (update-in editor key (buffer/delete buffer/char :left))))

(def delete
  #(update-in % keys/buffer (buffer/delete buffer/char :right)))

(def delete-right
  #(update-in % keys/buffer (buffer/delete buffer/line :right)))

(def delete-left
  #(update-in % keys/buffer (buffer/delete buffer/line :left)))

(def delete-line (comp backspace delete-right delete-left))

(def delete-right-word
  #(update-in % keys/buffer (buffer/delete buffer/word :right)))

(def delete-left-word
  #(update-in % keys/buffer (buffer/delete buffer/word :left)))

(def undo #(update-in % keys/window history/undo))

(def redo #(update-in % keys/window history/redo))

(defn commit
  ([editor] (commit editor keys/window))
  ([editor key] (update-in editor key history/commit)))

(def next-buffer #(update-in % keys/frame frame/next))

(def prev-buffer #(update-in % keys/frame frame/prev))

(defn notice [editor message]
  (assoc-in editor keys/minibuffer (buffer/read message)))

(defn open
  ([editor name] (open editor name ""))
  ([editor name string]
     (let [buffer (vary-meta (-> string buffer/read history/create)
                             assoc :parser (language/extension name))]
       (update-in editor keys/frame (frame/update name buffer)))))

(defn command [editor]
  (-> editor (get-in keys/minibuffer) buffer/command))

(defn run [editor key]
  (let [keymap (get-in editor keys/keymap)]
    (if-let [f (keymap key)]
      (f editor)
      ((:default keymap) editor key))))
