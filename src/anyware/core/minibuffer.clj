(ns anyware.core.minibuffer
  (:refer-clojure :exclude [char])
  (:require [anyware.core.api :refer [minibuffer] :as api]
            [anyware.core.buffer :refer [char append delete move]]))

(def backspace #(update-in % minibuffer (delete char :left)))

(def right #(update-in % minibuffer (move char :right)))

(def left #(update-in % minibuffer (move char :left)))

(def keymap
  {:backspace backspace
   :right right
   :left left
   :enter api/execute
   :default (api/insert minibuffer)})

(defn mode [editor mode]
  (assoc-in editor api/mode
            (assoc keymap :escape #(assoc-in % api/mode mode))))
