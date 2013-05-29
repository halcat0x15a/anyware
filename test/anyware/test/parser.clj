(ns anyware.test.parser
  (:require [clojure.test.generative :refer (defspec is)]
            [anyware.core.parser :as parser]))

(defspec character-parser
  parser/parse
  [^char char ^string string]
  (is (if (:result %)
        (= (:result %) char)
        (= (:next %) string))))
