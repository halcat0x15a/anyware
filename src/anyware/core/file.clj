(ns anyware.core.file
  (:require [anyware.core.lens :as lens]
            [anyware.core.lens.record :as record]
            [anyware.core.buffer.history :as history]
            [anyware.core.buffer.list :as list]
            [anyware.core.parser.language :as language]))

(defn open [path string editor]
  (lens/modify :list
               (partial list/add
                        path
                        (with-meta (history/read string)
                          {:parser (language/extension path)}))
               editor))

(defn save [editor]
  (lens/get record/entry editor))
