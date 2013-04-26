(ns anyware.jvm.file
  (:require [anyware.core.record :as record]
            [anyware.core.frame :as frame]
            [anyware.core.api.command :as command])
  (:import [javafx.stage FileChooser]))

(def ^FileChooser chooser (FileChooser.))

(defn open
  ([editor]
     (if-let [file (.showOpenDialog chooser (-> editor meta :stage))]
       (open (.getPath file) editor)
       editor))
  ([path editor]
     (update-in editor [:list] (partial frame/update path (slurp path)))))

(defmethod command/exec "open" [[_ file] editor]
  (if file (open editor) editor))

(defn save [editor]
  (doto editor
    (->> (record/get record/buffer) spit)))

(defmethod command/exec "save" [_ editor]
  (save editor))
