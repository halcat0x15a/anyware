(ns anyware.test.editor
  (:refer-clojure :exclude [type])
  (:require [clojure.test :refer (deftest is testing)]
            [anyware.core.editor :as editor]))

(defn type [& keys]
  (->> keys (map seq) flatten (reduce editor/run editor/editor) str))

(deftest editor
  (testing "insert 'hello world'"
    (is (= (type "ihello world") "hello world")))
  (testing "move cursor"
    (is (= (type "iworld" [:esc] "hhhhhihello ") "hello world"))))
