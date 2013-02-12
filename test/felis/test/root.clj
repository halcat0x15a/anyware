(ns felis.test.root
  (:require [clojure.test.generative :refer (defspec is)]
            [felis.root :as root]
            [felis.test :as test]))

(defspec add-remove
  (fn [root buffer]
    (->> root (root/add buffer) root/remove))
  [^test/root root ^test/buffer buffer]
  (is (= % root)))
