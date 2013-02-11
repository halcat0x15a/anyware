(ns felis.test
  (:refer-clojure :exclude [list])
  (:require [clojure.core :as core]
            [clojure.data.generators :as gen]
            [felis.edit :as edit]
            [felis.text :as text]
            [felis.buffer :as buffer]
            [felis.workspace :as workspace]
            [felis.root :as root]
            [felis.editor.normal :as normal]
            [felis.editor.insert :as insert]
            [felis.editor.delete :as delete]))

(defn list [f]
  (apply core/list (gen/list f)))

(defn text []
  (assoc text/default
    :lefts (gen/string)
    :rights (gen/string)))

(defn top []
  (gen/vec text))

(defn bottom []
  (list text))

(defn buffer []
  (assoc buffer/default
    :name (gen/keyword)
    :focus (text)
    :lefts (top)
    :rights (bottom)))

(defn workspace []
  (assoc workspace/default
    :buffer (buffer)))

(defn root []
  (assoc root/default
    :workspace (workspace)
    :minibuffer (text)))

(defn editor []
  ((gen/rand-nth [normal/->Normal
                  insert/->Insert
                  delete/->Delete])
   (root)))

(defprotocol Container
  (element [edit]))

(extend-protocol Container
  felis.buffer.Buffer
  (element [edit] (text))
  felis.text.Text
  (element [edit] (gen/char)))
