(ns anyware.jvm.file
  (:require [anyware.core.file :as file]
            [anyware.core.command :as command])
  (:import [javafx.stage FileChooser]))

(def chooser (FileChooser.))

(defn open
  ([editor]
     (open (.. chooser (showOpenDialog nil) getPath) editor))
  ([path editor]
     (file/open editor path (slurp path))))

(defmethod command/exec "open"
  [[_ & files] editor]
  (if (empty? files)
    (open editor)
    (doseq [file files]
      (open file editor))))

(defn save [editor]
  (doto editor
    (file/save spit)))

(defmethod command/exec "save" [_ editor]
  (file/save editor))
