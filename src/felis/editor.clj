(ns felis.editor
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [clojure.set :as set]
            [felis.html :as html]
            [felis.lisp.environment :as environment]
            [felis.workspace :as workspace]
            [felis.buffer :as buffer]))

(def environment (atom {}))

(defn add [workspace {:keys [current workspaces] :as editor}]
  (assoc editor
    :current workspace
    :workspaces (conj workspaces current)))

(defn remove [{:keys [current workspaces] :as editor}]
  (if-let [workspace (first workspaces)]
    (assoc editor
      :current workspace
      :workspaces (disj workspaces workspace))
    (assoc editor
      :current workspace/default)))

(defn change [name {:keys [current workspaces] :as editor}]
  (if-let [workspace (some #(= (:name %) name) workspaces)]
    (assoc editor
      :current workspace
      :workspaces (conj workspaces current))
    editor))

(defn run [editor]
  (let [[command & args]
        (-> editor
            :minibuffer
            buffer/write
            (string/split #" "))]
    (if-let [f (get @environment (symbol command))]
      (assoc (apply f editor args)
        :minibuffer buffer/default)
      editor)))

(defrecord Editor [current workspaces minibuffer mode])

(def default
  (Editor. workspace/default #{} buffer/default :normal))
