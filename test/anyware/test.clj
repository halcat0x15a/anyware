(ns anyware.test
  (:refer-clojure :exclude [list])
  (:require [clojure.data.generators :as gen]
            [anyware.core.buffer :as buffer]
            [anyware.core.buffer.history :as history]
            [anyware.core.buffer.list :as list]
            [anyware.core.editor :as editor]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string)))

(defn history []
  (history/create (buffer)))

(defn list []
  (list/create (gen/keyword) (history)))

(defn editor []
  (assoc editor/default
    :list (list)
    :minibuffer (buffer)))
