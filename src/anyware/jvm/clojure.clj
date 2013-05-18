(ns anyware.jvm.clojure
  (:require [anyware.core.api :as api]
            [anyware.core.keys :as keys]
            [anyware.core.buffer :as buffer]))

(defn eval-file [editor]
  (api/notice editor
              (-> editor
                  (get-in keys/buffer)
                  buffer/write
                  read-string
                  eval)))
