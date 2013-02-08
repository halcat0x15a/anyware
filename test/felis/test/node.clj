(ns felis.test.node
  (:require [clojure.test.generative :refer :all]
            [felis.test :as test]
            [felis.node :refer :all]))

(defspec render-string
  render
  [^test/node node]
  (is (string? %)))

(defspec get-in-path
  (fn [editor node]
    (->> node type path (get-in editor)))
  [^test/editor editor ^test/node node]
  (is (not (nil? %))))
