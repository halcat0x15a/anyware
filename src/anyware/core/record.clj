(ns anyware.core.record
  (:refer-clojure :exclude [name])
  (:require [anyware.core.lens :as lens]))

(def entry (lens/comp lens/zip :frame))

(def name (lens/comp :name entry))

(def history (lens/comp :value entry))

(def change (lens/comp lens/zip history))

(def buffer (lens/comp :value change))

(def minibuffer (->> :minibuffer
                     (lens/comp lens/zip)
                     (lens/comp :value)))
