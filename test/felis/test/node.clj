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

(defn node []
  ((gen/rand-nth
    [test/root
     test/workspace
     test/buffer
     test/text])))

(defn node? [x]
  (or (string? x)
      (vector? x)
      (instance? felis.html.Element x)))

(defspec path-get-in-editor
  get-in
  [^test/editor editor ^{:tag `path} path]
  (is (not (nil? %))))

(defspec render-node
  html/render
  [^{:tag `node} node]
  (is (node? %)))
