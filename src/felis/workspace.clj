(ns felis.workspace
  (:refer-clojure :exclude [name])
  (:require [felis.string :as string]
            [felis.serialization :as serialization]
            [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.syntax :as syntax]
            [felis.history :as history]))

(defrecord Workspace [name buffer history syntax])

(def default
  (Workspace. :*scratch* buffer/default history/default syntax/default))

(def path [:root :workspace])

(def name (conj path :name))
