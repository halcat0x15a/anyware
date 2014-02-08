(ns anyware.core.workspace)

(defrecord Workspace [current windows])

(defprotocol Window
  (id [window]))

(defn workspace [window]
  (Workspace. window {}))

(defn open [{:keys [current windows] :as workspace} window]
  (assoc workspace
    :current window
    :windows (assoc windows (id current) current)))

(defn select [{:keys [current windows] :as workspace} name]
  (if-let [{window true others false} (group-by #(= name (:name %)) windows)]
    (assoc workspace
      :current (first window)
      :windows (cons current others))
    workspace))
