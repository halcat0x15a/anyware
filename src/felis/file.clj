(ns felis.file
  (:require [felis.path :as path]
            [felis.language :as language]
            [felis.editor :as editor]
            [felis.workspace :as workspace]
            [felis.buffer :as buffer]))

(defn open [editor path content]
  (editor/add (workspace/create path
                                (buffer/read content)
                                (language/extension path))
              editor))

(defn save [editor save]
  (save (-> editor (get-in path/name) name)
        (-> editor (get-in path/buffer) buffer/write)))
