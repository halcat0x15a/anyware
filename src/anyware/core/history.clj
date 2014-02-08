(ns anyware.core.history
  (:require [clojure.zip :as zip]))

(defrecord Change [present future])

(defn change [value] (Change. value nil))

(defn- make-node [change children]
  (assoc change :future children))

(defn history [value]
  (zip/zipper :future :future make-node (change value)))

(defn undo [history]
  (if-let [history (zip/up history)]
    history
    (throw (ex-info "Already at oldest change" {}))))

(defn redo [history]
  (if-let [history (zip/down history)]
    history
    (throw (ex-info "Already at newest change" {}))))

(defn commit [history value]
  (-> history
      (zip/edit assoc :future [])
      (zip/insert-child (change value))
      zip/down))

(defn present [history]
  (-> history zip/node :present))
