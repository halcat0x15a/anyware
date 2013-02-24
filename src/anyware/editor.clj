(ns anyware.editor
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [anyware.html :as html]
            [anyware.lens :as lens]
            [anyware.buffer :as buffer]
            [anyware.history :as history]
            [anyware.mode :as mode]))

(defn add [name' history' {:keys [name history] :as editor}]
  (->> editor
       (lens/set lens/name name')
       (lens/set lens/history history')
       (lens/modify lens/buffers #(assoc % name history))))

(def environment
  (atom {}))

(defn run
  ([editor] (run @environment editor))
  ([environment editor]
     (let [[command & args]
           (-> editor
               :minibuffer
               buffer/write
               (string/split #" "))]
       (if-let [f (get environment command)]
         (assoc (apply f editor args)
           :minibuffer buffer/default)
         editor))))

(defrecord Editor [name history buffers minibuffer mode])

(def default
  (Editor. :*scratch* history/default {} buffer/default mode/normal))
