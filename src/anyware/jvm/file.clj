(ns anyware.jvm.file
  (:require [anyware.core.frame :as frame]
            [anyware.core.api :as api])
  (:import [javafx.stage FileChooser]))

(def ^FileChooser chooser (FileChooser.))

(defn open
  ([editor]
     (if-let [file (.showOpenDialog chooser (-> editor meta :stage))]
       (open (.getPath file) editor)
       editor))
  ([path editor]
     (update-in editor [:list] (partial frame/update path (slurp path)))))

(defn save [editor]
  (doto editor
    (->> (get-in api/buffer) spit)))
