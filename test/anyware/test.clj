(ns anyware.test
  (:refer-clojure :exclude [list])
  (:require [clojure.data.generators :as gen]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.parser :as parser]
            [anyware.core.editor :as editor]))

(defn buffer []
  (vary-meta (buffer/->Buffer (gen/string) (gen/string))
             assoc :parser parser/id))

(defn history []
  (history/create (buffer)))

(defn frame []
  (frame/create (gen/string) (history)))

(defn editor []
  (with-meta
    (assoc editor/default
      :frame (frame)
      :command (history))
    {:height (gen/byte)}))
