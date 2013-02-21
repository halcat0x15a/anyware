(ns felis.test.parser
  (:require [clojure.test.generative :refer (defspec is)]
            [felis.parser :as parser]))

(defspec literal-parser
  parser/literal
  [^string string]
  (is (= (:result (% string)) string)))
