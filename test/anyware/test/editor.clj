(ns anyware.test.editor
  (:require [clojure.test.generative :refer (defspec is)]
            [anyware.test :as test]
            [anyware.editor :as editor]))

(defspec add-remove
  (fn [editor buffer]
    (->> editor (editor/add buffer) editor/remove))
  [^test/editor editor ^test/buffer buffer]
  (is (= % editor)))
