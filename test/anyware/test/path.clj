(ns anyware.test.path
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.path :as path]))

(defn path []
  (gen/rand-nth
   [path/current
    path/name
    path/history
    path/buffer
    path/minibuffer]))

(defspec path-get-in-editor
  get-in
  [^test/editor editor ^{:tag `path} path]
  (is (not (nil? %))))
