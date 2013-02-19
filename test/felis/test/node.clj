(ns felis.test.node
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [felis.test :as test]
            [felis.html :as html]
            [felis.root :as root]
            [felis.workspace :as workspace]
            [felis.history :as history]
            [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.editor.normal :as normal]))

(defn path []
  (gen/rand-nth
   [root/path
    workspace/path
    workspace/name
    history/path
    buffer/path
    text/path
    text/minibuffer]))

(defspec path-get-in-editor
  get-in
  [^test/editor editor ^{:tag `path} path]
  (is (not (nil? %))))
