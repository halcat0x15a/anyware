(ns anyware.core.window)

(defrecord Window [saved? name value])

(def create (partial ->Window true))

(defn save [window]
  (assoc window :save? true))

(defn update [f window]
  (-> window
      (update-in [:value] f)
      (assoc :save? false)))
