(ns anyware.test.parser
  (:require [clojure.test.generative :refer (defspec is)]
            [anyware.core.parser :as parser]))

(defspec literal-parser
  parser/literal
  [^string string]
  (is (= (:result (% string)) string)))
