(ns felis.keymap
  (:require [clojure.walk :as walk]
            [felis.path :as path]
            [felis.editor :as editor]
            [felis.history :as history]
            [felis.buffer :as buffer]))

(defrecord Keymap [map input])

(defn map-values [f keymap]
  (walk/walk (fn [[k v]] [k (f v)]) identity keymap))

(defn update-values [path keymap]
  (map-values (fn [f]
                (fn [editor]
                  (update-in editor path f)))
              keymap))

(defn nothing [editor char] editor)

(defn append [path]
  (fn [editor char]
    (update-in editor
               path
               (partial buffer/append char))))

(def arrows
  (update-values path/buffer
                 {:left buffer/left
                  :right buffer/right
                  :up buffer/up
                  :down buffer/down}))

(def insert
  (Keymap. (merge arrows
                  (update-values path/buffer
                                 {:backspace buffer/backspace
                                  :enter buffer/break}))
           (append path/buffer)))

(def normal
  (Keymap. (merge arrows
                  (update-values path/buffer
                                 {\h buffer/left
                                  \j buffer/down
                                  \k buffer/up
                                  \l buffer/right
                                  \0 buffer/head
                                  \9 buffer/tail
                                  \x buffer/delete
                                  \X buffer/backspace})
                  (map-values (fn [f]
                                (fn [editor]
                                  (-> editor
                                      (update-in path/buffer f)
                                      (assoc :mode :insert))))
                              {\a buffer/right
                               \I buffer/head
                               \A buffer/tail
                               \o buffer/newline
                               \O buffer/return
                               \i identity})
                  (update-values path/history
                                {\u history/undo
                                 \r history/redo})
                  {\d #(assoc % :mode :delete)
                   \: #(assoc % :mode :minibuffer)})
           nothing))

(def delete
  (Keymap. (update-values path/buffer
                          {\h buffer/backspace
                           \l buffer/delete
                           \d buffer/delete})
           nothing))

(def minibuffer
  (Keymap. (merge {:enter editor/run}
                  (update-values path/minibuffer
                                 {:backspace buffer/backspace
                                  :left buffer/left
                                  :right buffer/right}))
           (append path/minibuffer)))

(def keymap
  (atom {:normal normal
         :insert insert
         :delete delete
         :minibuffer minibuffer}))
