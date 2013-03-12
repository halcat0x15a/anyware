(ns anyware.core.mode
  (:require [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.buffer.character :as character]
            [anyware.core.mode.insert :as insert]
            [anyware.core.mode.delete :as delete]
            [anyware.core.mode.minibuffer :as minibuffer]
            [anyware.core.mode.normal :as normal]))

(defmulti keymap identity)
(defmethod keymap :insert [_] @insert/keymap)
(defmethod keymap :delete [_] @delete/keymap)
(defmethod keymap :minibuffer [_] @minibuffer/keymap)
(defmethod keymap :normal [_] @normal/keymap)
(defmethod keymap :default [_] {})

(defn append [lens key editor]
  (lens/modify lens (partial character/append key) editor))

(defmulti default (fn [mode _ _] mode))
(defmethod default :insert [_ key editor]
  (append record/buffer key editor))
(defmethod default :minibuffer [_ key editor]
  (append record/minibuffer key editor))
(defmethod default :default [_ _ editor] editor)
