(ns felis.workspace
  (:refer-clojure :exclude [name])
  (:require [felis.buffer :as buffer]
            [felis.language :as language]
            [felis.history :as history]))

(defrecord Workspace [name buffer history language])

(def default
  (Workspace. :*scratch* buffer/default history/default language/text))

(defn create [name buffer language]
  (assoc default
    :name name
    :buffer buffer
    :language language))

(defn commit [{:keys [buffer history] :as workspace}]
  (update-in workspace [:history] (partial history/commit buffer)))

(defn undo [{:keys [buffer history] :as workspace}]
  (if-let [history (history/undo history)]
    (assoc workspace
      :history history
      :buffer (:present history))
    workspace))

(defn redo [{:keys [buffer history] :as workspace}]
  (if-let [history (history/redo history)]
    (assoc workspace
      :history history
      :buffer (:present history))
    workspace))
