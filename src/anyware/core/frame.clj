(ns anyware.core.frame
  (:refer-clojure :exclude [find remove conj])
  (:require [clojure.zip :as zip]))

(defn init [name obj]
  (vary-meta obj assoc
             :name name
             :saved? true))

(defn create [& keyvals]
  (->> keyvals
       (partition 2)
       (map (partial apply init))
       vec
       zip/vector-zip
       zip/down))

(defn find
  ([name] (partial find name))
  ([name frame]
     (loop [frame' (-> frame zip/root zip/vector-zip)]
       (cond (zip/end? frame')
             (vary-meta frame assoc
                        :error (str "No matching buffer for " name))
             (identical? name (-> frame' zip/node meta :name)) frame'
             :else (recur (zip/next frame'))))))

(defn remove [frame]
  (if (-> frame zip/node meta :saved?)
    (zip/remove frame)
    (vary-meta frame assoc
               :error "No write since last change")))

(defn conj [value frame]
  (-> frame (zip/insert-right value) zip/right))

(defn update
  ([name value] (partial update name value))
  ([name value frame]
     (let [frame' (find name frame)
           frame'' (remove frame')
           value' (vary-meta value assoc :name name)]
       (cond (-> frame' meta :error) (conj value' frame)
             (-> frame'' meta :error) frame''
             :else (conj value' frame'')))))
