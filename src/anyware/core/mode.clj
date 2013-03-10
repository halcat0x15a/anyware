(ns anyware.core.mode
  (:refer-clojure :exclude [set])
  (:require [anyware.core.lens.record :as record]))

(defmulti keymap identity)
(defmethod keymap :default [_] {})

(defmulti default identity)
(defmethod default :default [_] (fn [editor] (constantly editor)))

(defn set [mode]
  (record/with :mode (constantly mode)))
