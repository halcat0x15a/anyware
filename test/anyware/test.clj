(ns anyware.test
  (:refer-clojure :exclude [list])
  (:require [clojure.data.generators :as gen]
            [anyware.buffer :as buffer]
            [anyware.history :as history]
            [anyware.editor :as editor]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string)))

(defn history []
  (history/create (buffer)))

(defn buffers []
  (gen/hash-map gen/keyword history))

(defn editor []
  (assoc editor/default
    :buffers (buffers)
    :minibuffer (buffer)))
