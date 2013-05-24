(ns anyware.core.keys
  (:require [anyware.core.history :as history]))

(def frame [:frame])

(def window (conj frame 0))

(def buffer (into window history/current))

(def command [:command])

(def minibuffer (into command history/current))

(def keymap [:keymap])

(def clipboard [:clipboard])

(def clip (conj clipboard 0))

(def all
  [frame
   window
   buffer
   keymap
   command
   minibuffer
   clipboard
   clip])

(defn validate [editor] (every? (partial get-in editor) all))
