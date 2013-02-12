(ns felis.test.root
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.xml :as xml]
            [felis.root :as root]
            [felis.test :as test])
  (:import [java.io ByteArrayInputStream]))

(defspec add-remove
  (fn [root buffer]
    (->> root (root/add buffer) root/remove))
  [^test/root root ^test/buffer buffer]
  (is (= % root)))

(defspec valid-html
  root/render
  [^test/root root]
  (is (-> % .getBytes ByteArrayInputStream. xml/parse)))
