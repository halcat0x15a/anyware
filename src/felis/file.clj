(ns felis.file
  (:require [felis.path :as path]
            [felis.serialization :as serialization]
            [felis.language :as language]
            [felis.root :as root]
            [felis.workspace :as workspace]
            [felis.buffer :as buffer]))

(defn open [editor path content]
  (update-in editor
             path/root
             (partial root/add
                      (assoc workspace/default
                        :name path
                        :buffer (buffer/read content)
                        :language (language/extension path)))))

(defn save [editor save]
  (save (-> editor
            (get-in path/name)
            name)
        (-> editor
            (get-in path/buffer)
            serialization/write)))
