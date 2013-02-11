(ns felis.test
  (:refer-clojure :exclude [list])
  (:require [clojure.core :as core]
            [clojure.data.generators :as gen]
            [felis.edit :as edit]
            [felis.text :as text]
            [felis.minibuffer :as minibuffer]
            [felis.buffer :as buffer]
            [felis.root :as root]
            [felis.default :as default]
            [felis.editor.normal :as normal]
            [felis.editor.insert :as insert]
            [felis.editor.delete :as delete]))

(defn list [f]
  (apply core/list (gen/list f)))

(defn text []
  (assoc text/default
    :lefts (gen/string)
    :rights (gen/string)))

(defn minibuffer []
  (assoc minibuffer/default
    :text (text)))

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

(defn edit []
  (gen/rand-nth [(text) (buffer)]))

(defn root []
  (assoc root/default
    :buffer (buffer)
    :minibuffer (minibuffer)))

(defn node []
  (rand-nth [(root) (buffer) (minibuffer) (text)]))

(defn editor []
  (gen/rand-nth [(normal/->Normal (root))
                 (insert/->Insert (root))
                 (delete/->Delete (root))]))

(defn field []
  (gen/rand-nth [:lefts :rights]))

(defprotocol Container
  (element [edit]))

(extend-protocol Container
  felis.buffer.Buffer
  (element [edit] (text))
  felis.text.Text
  (element [edit] (gen/char)))
