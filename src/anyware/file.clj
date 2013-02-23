(ns anyware.file
  (:require [anyware.path :as path]
            [anyware.language :as language]
            [anyware.editor :as editor]
            [anyware.workspace :as workspace]
            [anyware.buffer :as buffer]))

(defn open [editor path content]
  (editor/add (workspace/create path
                                (buffer/read content)
                                (language/extension path))
              editor))

(defn save [editor save]
  (save (-> editor (get-in path/name) name)
        (-> editor (get-in path/buffer) buffer/write)))
