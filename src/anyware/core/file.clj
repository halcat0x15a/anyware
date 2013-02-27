(ns anyware.core.file
  (:require [anyware.core.lens :as lens]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.parser :as parser]
            [anyware.core.parser.clojure :as clj]))

(defmulti extension
  (let [extension #"\.(\w+)$"]
    (comp second (partial re-find extension))))
(defmethod extension "clj" [_] clj/expressions)
(defmethod extension :default [_] parser/text)

(defn open [editor path content]
  (editor/add path (-> content buffer/read history/create) editor))

(defn save [editor save]
  (save (->> editor (lens/get lens/name) name)
        (->> editor (lens/get lens/buffer) buffer/write)))
