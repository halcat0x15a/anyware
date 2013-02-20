(ns felis.root
  (:refer-clojure :exclude [remove find])
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [felis.html :as html]
            [felis.lisp.environment :as environment]
            [felis.workspace :as workspace]
            [felis.edit :as edit]
            [felis.text :as text]))

(defn add [workspace {:keys [current workspaces] :as root}]
  (assoc root
    :current workspace
    :workspaces (conj workspaces current)))

(defn remove [{:keys [current workspaces] :as root}]
  (if-let [workspace (first workspaces)]
    (assoc root
      :current workspace
      :workspaces (disj workspaces workspace))
    (assoc root
      :current workspace/default)))

(defn change [name {:keys [current workspaces] :as root}]
  (if-let [workspace (some #(= (:name %) name) workspaces)]
    (assoc root
      :current workspace
      :workspaces (conj workspaces current))
    root))

(defrecord Root [current workspaces minibuffer environment style settings])

(def path [:root])

(def environment (conj path :environment))

(def default
  (Root. workspace/default
         #{}
         text/default
         environment/global
         html/style
         {}))
