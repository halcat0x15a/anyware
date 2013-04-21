(ns anyware.core.frame
  (:refer-clojure :exclude [find remove conj])
  (:require [clojure.zip :as zip])
  (:import clojure.lang.ExceptionInfo))

(defn window [name obj] (vary-meta obj assoc :name name :saved? true))

(defn create [& keyvals]
  (->> keyvals
       (partition 2)
       (map (partial apply window))
       vec
       zip/vector-zip
       zip/down))

(defn find
  ([name] (partial find name))
  ([name frame]
     (loop [frame' (-> frame zip/root zip/vector-zip)]
       (cond (zip/end? frame')
             (throw (ex-info (str "No matching buffer for " name)
                             {:name name}))
             (identical? name (-> frame' zip/node meta :name)) frame'
             :else (recur (zip/next frame'))))))

(defn remove [frame]
  (if (-> frame zip/node meta :saved?)
    (zip/remove frame)
    (throw (ex-info "No write since last change" {}))))

(defn save [frame] (vary-meta frame assoc :save? true))

(defn conj [value frame]
  (-> frame (zip/insert-right value) zip/right))

(defn update
  ([name value] (partial update name value))
  ([name value frame]
     (let [value (vary-meta value assoc :name name)]
       (try
         (->> frame (find name) remove (conj value))
         (catch ExceptionInfo e
           (if (-> e ex-data :name (identical? name))
             (conj value frame)
             (throw e)))))))
