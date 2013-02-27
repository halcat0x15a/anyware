(ns anyware.jvm.file
  (:require [anyware.core.file :as file])
  (:import [javafx.stage FileChooser]))

(def chooser (FileChooser.))

(defn open
  ([editor]
     (open editor (.. chooser (showOpenDialog nil) getPath)))
  ([editor path]
     (file/open editor path (slurp path))))

(defn save [editor]
  (doto editor
    (file/save spit)))
