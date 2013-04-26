(ns anyware.core.api.file
  (:require [anyware.core.api :only (frame)]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.language :as language]))

(defn open [path string editor]
  (update-in editor frame
             (partial frame/assoc path
                      (with-meta (-> string buffer/read history/create)
                        {:parser (language/extension path)}))))
