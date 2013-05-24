(ns anyware.test
  (:refer-clojure :exclude [list])
  (:require [clojure.data.generators :as gen]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.parser :as parser]
            [anyware.core.editor :as editor]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string)))

(defn history []
  (vary-meta (history/create (buffer)) assoc :parser parser/id))

(defn frame []
  (frame/create (gen/string) (history)))

(defn editor []
  (with-meta
    (assoc editor/default
      :frame (frame)
      :command (history))
    {:height (gen/byte)}))
