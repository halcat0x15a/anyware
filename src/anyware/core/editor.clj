(ns anyware.core.editor
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.zip :as zip]
            [anyware.core.html :as html]
            [anyware.core.command :as command]
            [anyware.core.lens :as lens]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.mode :as mode]))

(defn add [name' history' {:keys [name history] :as editor}]
  (->> editor
       (lens/set lens/name name')
       (lens/set lens/history history')
       (lens/modify lens/buffers #(assoc % name history))))

(defrecord Editor [name history buffers minibuffer mode])

(def default
  (Editor. :*scratch* history/default {} buffer/default mode/normal))
