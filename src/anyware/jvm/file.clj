(ns anyware.jvm.file
  (:require [clojure.string :as string]
            [clojure.java.io :as io]
            [anyware.core.frame :as frame]
            [anyware.core.api :as api])
  (:import java.io.File
           javafx.stage.FileChooser))

(def ^FileChooser chooser (FileChooser.))

(defn open-file [editor ^File file]
  (api/open editor (.getPath file) (slurp file)))

(defn open-directory [editor ^File file]
  (api/open editor
            (.getPath file)
            (string/join \newline (.listFiles file))))

(defn open
  ([editor]
     (if-let [file (.showOpenDialog chooser (-> editor meta :stage))]
       (open editor file)
       editor))
  ([editor path]
     (let [^File file (io/as-file path)]
       (if (.isDirectory file)
         (open-directory editor file)
         (open-file editor file)))))

(defn save [editor]
  (doto editor
    (->> (get-in api/buffer) spit)))
