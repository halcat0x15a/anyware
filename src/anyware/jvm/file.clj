(ns anyware.jvm.file
  (:require [anyware.core.buffer.list :as list]
            [anyware.core.lens :as lens]
            [anyware.core.command :as command])
  (:import [javafx.stage FileChooser]))

(def chooser (FileChooser.))

(defn open
  ([editor]
     (if-let [file (.showOpenDialog chooser (-> editor meta :stage))]
       (open (.getPath file) editor)
       editor))
  ([path editor]
     (update-in editor [:list] (partial list/add path (slurp path)))))

(defmethod command/exec "open" [[_ file] editor]
  (if file (open editor) editor))

(defn save [editor]
  (doto editor
    (->> (lens/get lens/buffer) spit)))

(defmethod command/exec "save" [_ editor]
  (save editor))
