(ns anyware.core.window)

(def ^:dynamic *messages*)

(defn current
  ([frame]
     (get frame (-> frame meta :current)))
  ([frame name]
     (vary-meta frame assoc :current name)))
  
(defn open [frame name window & entries]
  (-> (apply assoc frame name window entries)
      (current name)))

(defn edit [frame f & args]
  (update-in frame [(-> frame meta :current)]
    #(vary-meta (apply f % args) assoc :changed? true)))

(defn close [frame]
  (if (-> frame current meta :changed?)
    (assoc frame *messages* "No write")
    (dissoc frame (-> frame meta :current))))

(defn save [frame]
  (update-in frame [(-> frame meta :current)] vary-meta dissoc :changed?))
