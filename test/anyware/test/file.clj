(ns anyware.test.file
  (:require [clojure.test.generative :refer :all]
            [clojure.test :refer (are)]
            [anyware.test :as test]
            [anyware.core.file :as file]))

(defspec open-save
  (fn [editor path content]
    (-> editor (file/open path content) (file/save list)))
  [^test/editor editor ^string path ^string content]
  (are [x y] (= x y)
       (first %) path
       (second %) content))
