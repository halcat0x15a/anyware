(ns anyware.core.file
  (:refer-clojure :exclude [read])
  (:require [clojure.string :as string]
            [anyware.core.api :as api]
            [anyware.core.keys :as keys]))

(defprotocol IO
  (dialog [io])
  (read [io file])
  (write [io file value]))

(defprotocol Data
  (content [data]))

(defrecord File [path content]
  Data
  (content [file] content))

(defrecord Directory [path files]
  Data
  (content [dir] (string/join \newline files)))

(defn open
  ([editor]
     (if-let [{:keys [path content]} (dialog editor)]
       (api/open editor path content)
       editor))
  ([editor path]
     (let [file (read editor path)]
       (api/open editor (:path file) (content file)))))

(defn save [editor]
  (-> editor
      (write (-> editor (get-in keys/window) meta :name)
             (api/text editor))
      (update-in keys/window #(vary-meta assoc :save? true))))
