(ns anyware.core.minibuffer
  (:refer-clojure :exclude [char])
  (:require [anyware.core.api :as api]
            [anyware.core.keys :as keys]
            [anyware.core.buffer :refer [char append delete move]]))

(def backspace #(update-in % keys/minibuffer (delete char :left)))

(def right #(update-in % keys/minibuffer (move char :right)))

(def left #(update-in % keys/minibuffer (move char :left)))

(def keymap
  {:backspace backspace
   :right right
   :left left
   :enter api/execute
   :default (api/insert keys/minibuffer)})

(defn mode [editor mode]
  (assoc-in editor keys/mode
            (assoc keymap
              :escape #(assoc-in % keys/mode mode))))
