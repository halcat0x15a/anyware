(ns felis.workspace
  (:refer-clojure :exclude [name])
  (:require [clojure.core :as core]
            [felis.html :as html]
            [felis.buffer :as buffer]
            [felis.syntax :as syntax]
            [felis.history :as history]))

(defn render [{:keys [name buffer syntax]}]
  [(html/->Node :p {:class :status} (core/name name))
   (buffer/render syntax buffer)])

(defrecord Workspace [name buffer history syntax])

(def path [:root :workspace])

(def name (conj path :name))

(def default
  (Workspace. :*scratch* buffer/default history/default syntax/default))
