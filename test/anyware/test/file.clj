(ns anyware.test.file
  (:require [clojure.test.generative :refer :all]
            [clojure.test :refer (are)]
            [anyware.file :as file]
            [anyware.test :as test]))

(defspec open-save
  (fn [editor path content]
    (-> editor (file/open path content) (file/save list)))
  [^test/editor editor ^string path ^string content]
  (are [x y] (= x y)
       (first %) path
       (second %) content))
