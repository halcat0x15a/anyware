(ns anyware.core.editor
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [clojure.zip :as zip]
            [anyware.core.html :as html]
            [anyware.core.command :as command]
            [anyware.core.lens :as lens]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]
            [anyware.core.mode :as mode]))

(defn add [name' history' {:keys [name history] :as editor}]
  (->> editor
       (lens/set lens/name name')
       (lens/set lens/history history')
       (lens/modify lens/buffers #(assoc % name history))))

(defn change [name' {:keys [name history buffers] :as editor}]
  (if-let [history' (get buffers name')]
    (dissoc (add name' history' editor) name)
    editor))

(defmethod command/exec "buffer" [[_ name] editor] (change name editor))

(defrecord Editor [name history buffers minibuffer mode])

(def default
  (Editor. "*scratch*" history/default {} buffer/default mode/normal))
