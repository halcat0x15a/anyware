(ns anyware.core.lens.record
  (:refer-clojure :exclude [comp name])
  (:require [anyware.core.lens :as lens]))

(def entry (lens/comp lens/zip :list))

(def name (lens/comp :name entry))

(def history (lens/comp :history entry))

(def change (lens/comp lens/zip history))

(def buffer (lens/comp :buffer change))

(def minibuffer (lens/comp lens/zip :minibuffer))
