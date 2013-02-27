(ns anyware.test
  (:refer-clojure :exclude [list])
  (:require [clojure.data.generators :as gen]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.editor :as editor]
            [anyware.core.parser :as parser]
            [anyware.core.parser.clojure :as clj]))

(defn parser []
  (gen/rand-nth [parser/text clj/expressions]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string) (parser)))

(defn history []
  (history/create (buffer)))

(defn buffers []
  (gen/hash-map gen/keyword history))

(defn editor []
  (assoc editor/default
    :name (gen/string)
    :history (history)
    :buffers (buffers)
    :minibuffer (buffer)))
