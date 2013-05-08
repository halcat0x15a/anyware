(ns anyware.core.minibuffer
  (:refer-clojure :exclude [char])
  (:require [anyware.core.api :refer [minibuffer] :as api]
            [anyware.core.buffer :refer [char append delete move]]))

(def backspace #(update-in % minibuffer (delete char :left)))

(def right #(update-in % minibuffer (move char :right)))

(def left #(update-in % minibuffer (move char :left)))

(defn insert [editor key]
  (update-in editor minibuffer (partial append :left key)))

(def keymap
  {:backspace backspace
   :right right
   :left left
   :enter api/execute
   :default insert})

(def mode #(assoc-in % api/mode keymap))
