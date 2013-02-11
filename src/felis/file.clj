(ns felis.file
  (:require [felis.root :as root]
            [felis.buffer :as buffer]
            [felis.syntax :as syntax]
            [felis.syntax.clojure :as clojure]))

(def syntaxes
  {#".clj$" clojure/syntax})

(defn syntax [name]
  (let [syntaxes (filter (fn [[regex _]] (re-find regex name)) syntaxes)]
    (if (empty? syntaxes)
      syntax/default
      (-> syntaxes first second))))

(defn open [editor path content]
  (update-in editor
             root/path
             (partial root/add
                      (assoc (buffer/read content)
                        :path path
                        :syntax (syntax path)))))

(defn save [editor save]
  (save (-> editor
            (get-in (conj buffer/path :path))
            name)
        (-> editor
            (get-in buffer/path)
            buffer/write)))
