(ns anyware.file
  (:require [anyware.lens :as lens]
            [anyware.editor :as editor]
            [anyware.buffer :as buffer]
            [anyware.history :as history]))

(defn open [editor path content]
  (editor/add path (-> content buffer/read history/create) editor))

(defn save [editor save]
  (save (->> editor (lens/get lens/name) name)
        (->> editor (lens/get lens/buffer) buffer/write)))

