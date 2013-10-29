(ns anyware.jvm.io
  (:require [clojure.java.io :refer (as-file)]
            [anyware.core.buffer :as buffer]
            [anyware.core.window :as window]))

(defn open [editor path]
  (let [file (as-file path)]
    (cond (.isFile file) (update-in editor [:buffers] window/open path (buffer/->Viewer (slurp file) 0))
          :else editor)))

(defn write [{:keys [buffers] :as editor}]
  (slurp (-> buffers meta :current)
         (-> buffers window/current buffer/show))
  editor)
