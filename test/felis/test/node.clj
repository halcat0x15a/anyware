(ns felis.test.node
  (:require [clojure.test :refer :all]
            [felis.root :as root]
            [felis.workspace :as workspace]
            [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.editor.normal :as normal]))

(deftest path
  (testing "get in editor"
    (are [path] (not (nil? (get-in normal/default path)))
         root/path
         workspace/path
         workspace/name
         buffer/path
         text/path
         text/minibuffer)))
