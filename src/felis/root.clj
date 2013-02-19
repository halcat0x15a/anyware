(ns felis.root
  (:refer-clojure :exclude [remove find])
  (:require [clojure.string :as string]
            [felis.html :as html]
            [felis.lisp.environment :as environment]
            [felis.workspace :as workspace]
            [felis.edit :as edit]
            [felis.text :as text]))

(defn add [workspace' {:keys [workspace workspaces] :as root}]
  (assoc root
    :workspace workspace'
    :workspaces (conj workspaces workspace)))

(defn remove [{:keys [workspace workspaces] :as root}]
  (if-let [workspace' (first workspaces)]
    (assoc root
      :workspace workspace'
      :workspaces (disj workspaces workspace'))
    root))

(defrecord Root [workspace
                 workspaces
                 minibuffer
                 environment
                 style
                 settings])

(def path [:root])

(def environment (conj path :environment))

(def default
  (Root. workspace/default
         #{}
         text/default
         environment/global
         html/style
         {}))
