(ns anyware.core.record
  (:refer-clojure :exclude [name])
  (:require [anyware.core.lens :refer (comp zip)]))

(def entry (comp zip :frame))

(def name (comp :name entry))

(def history (comp :value entry))

(def saved (comp :saved entry))

(def change (comp zip history))

(def buffer (comp :value change))

(def minibuffer (->> :minibuffer (comp zip) (comp :value)))
