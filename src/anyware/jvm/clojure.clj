(ns anyware.jvm.clojure
  (:require [anyware.core.api :as api]
            [anyware.core.buffer :as buffer]))

(defn eval-file [editor]
  (api/notice editor
              (-> editor
                  (get-in api/buffer)
                  buffer/write
                  read-string
                  eval)))
