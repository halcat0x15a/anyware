(ns anyware.jvm.file
  (:require [anyware.core.file :as file]
            [anyware.core.command :as command])
  (:import [javafx.stage FileChooser]))

(def chooser (FileChooser.))

(defn open
  ([editor]
     (if-let [file (.showOpenDialog chooser (-> editor meta :stage))]
       (open (.getPath file) editor)
       editor))
  ([path editor]
     (file/open editor path (slurp path))))

(defmethod command/exec "open" [[_ file] editor]
  (if file (open editor) editor))

(defn save [editor]
  (doto editor
    (file/save spit)))

(defmethod command/exec "save" [_ editor]
  (file/save editor))
