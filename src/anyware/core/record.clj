(ns anyware.core.record
  (:refer-clojure :exclude [name])
  (:require [anyware.core.lens :refer (comp zip)]))

(def window (comp zip :frame))

(def name (comp :name window))

(def history (comp :value window))

(def change (comp zip history))

(def buffer (comp :value change))

(def minibuffer (->> :minibuffer (comp zip) (comp :value)))
