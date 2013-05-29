(ns anyware.test.parser
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.core.parser :as parser]))

(defn literal []
  ((rand-nth [gen/string gen/char])))

(defspec literal-parser
  parser/parse
  [^{:tag `literal} literal ^string input]
  (is (if-let [value (:value %)]
        (= value literal)
        (= (:next %) input))))
