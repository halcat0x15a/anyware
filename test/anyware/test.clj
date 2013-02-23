(ns anyware.test
  (:refer-clojure :exclude [list])
  (:require [clojure.data.generators :as gen]
            [anyware.buffer :as buffer]
            [anyware.history :as history]
            [anyware.workspace :as workspace]
            [anyware.editor :as editor]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string)))

(defn history []
  (assoc history/default
    :present (buffer)
    :past ((gen/rand-nth [(constantly nil) history]))
    :futures ((gen/rand-nth [(constantly []) (comp vector history)]))))

(defn workspace []
  (assoc workspace/default
    :name (gen/keyword)
    :buffer (buffer)))

(defn editor []
  (assoc editor/default
    :current (workspace)
    :minibuffer (buffer)))
