(ns anyware.core.system)

(defprotocol Application
  (write [app file content])
  (read [app file])
  (exit [app]))

(defrecord File [path content])

(defrecord Directory [path files])

(defn open [{:keys [ file]
  (update-in editor [:buffers] window/open file (buffer/->Viewer (read *system* (str (:directory editor) file)) 0)))
