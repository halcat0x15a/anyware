(ns felis.file
  (:require [felis.path :as path]
            [felis.serialization :as serialization]
            [felis.syntax :as syntax]
            [felis.syntax.clojure :as clojure]
            [felis.root :as root]
            [felis.workspace :as workspace]
            [felis.buffer :as buffer]))

(def syntaxes
  {#".clj$" clojure/syntax})

(defn syntax [name]
  (let [syntaxes (filter (fn [[regex _]] (re-find regex name)) syntaxes)]
    (if (empty? syntaxes)
      syntax/default
      (-> syntaxes first second))))

(defn open [editor path content]
  (update-in editor
             path/root
             (partial root/add
                      (assoc workspace/default
                        :name path
                        :buffer (buffer/read content)
                        :syntax (syntax path)))))

(defn save [editor save]
  (save (-> editor
            (get-in path/name)
            name)
        (-> editor
            (get-in path/buffer)
            serialization/write)))
