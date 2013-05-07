(ns anyware.core.file
  (:require [anyware.core.path :as path]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.language :as language]))

(defn open [path string editor]
  (update-in editor path/frame
             (partial frame/update path
                      (with-meta (-> string buffer/read history/create)
                        {:parser (language/extension path)}))))
