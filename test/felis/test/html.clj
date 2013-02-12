(ns felis.test.html
  (:require [clojure.test.generative :refer (defspec is)]
            [felis.html :as html]))

(defspec not-contains-lt-and-rl
  (comp set html/escape)
  [^string string]
  (is (and (not (contains? \< %))
           (not (contains? \> %)))))
