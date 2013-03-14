(ns anyware.core.file
  (:require [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.language :as language]))

(defn open [path string editor]
  (lens/modify :frame
               (partial frame/assoc
                        path
                        (with-meta (-> string
                                       buffer/read
                                       history/create)
                          {:parser (language/extension path)}))
               editor))

(defn save [editor]
  (lens/get record/entry editor))
