(ns anyware.core.mode
  (:refer-clojure :exclude [set])
  (:require [anyware.core.lens.record :as record]))

(defmulti keymap identity)
(defmethod keymap :default [_] {})

(defmulti default (fn [mode _ _] mode))
(defmethod default :default [_ _ editor] editor)
