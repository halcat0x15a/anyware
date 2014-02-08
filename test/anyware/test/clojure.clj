(ns anyware.test.clojure
  (:require [clojure.test :refer :all]
            [anyware.core.clojure :as clojure]
            [anyware.core.parser :refer :all]))

(deftest expression
  (testing "parse definition"
    (is (= (:value (clojure/expression "(def foo :bar)"))
           ["(" [(->Label :special "def") " " (->Label :symbol "foo")] " " (->Label :keyword ":bar") ")"]))))
           
