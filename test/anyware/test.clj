(ns anyware.test
  (:refer-clojure :exclude [list])
  (:require [clojure.data.generators :as gen]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.editor :as editor]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string)))

(defn history []
  (history/create (buffer)))

(defn window []
  (frame/->Saved (gen/string) (history)))

(defn frame []
  (frame/create (window)))

(defn editor []
  (with-meta
    (assoc editor/default
      :frame (frame)
      :minibuffer (history))
    {:height (gen/byte)}))
