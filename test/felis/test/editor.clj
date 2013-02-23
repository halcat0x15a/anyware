(ns felis.test.editor
  (:require [clojure.test.generative :refer (defspec is)]
            [felis.test :as test]
            [felis.editor :as editor]))

(defspec add-remove
  (fn [editor buffer]
    (->> editor (editor/add buffer) editor/remove))
  [^test/editor editor ^test/buffer buffer]
  (is (= % editor)))
