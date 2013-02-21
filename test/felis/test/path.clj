(ns felis.test.path
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [felis.test :as test]
            [felis.path :as path]))

(defn path []
  (gen/rand-nth
   [path/root
    path/current
    path/name
    path/history
    path/buffer
    path/focus
    path/minibuffer]))

(defspec path-get-in-editor
  get-in
  [^test/editor editor ^{:tag `path} path]
  (is (not (nil? %))))
