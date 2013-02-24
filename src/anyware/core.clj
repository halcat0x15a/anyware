(ns anyware.core)

(defprotocol Anyware
  (code [this event]))

(defn run [anyware event {:keys [mode] :as editor}]
  ((mode (code anyware event)) editor))
