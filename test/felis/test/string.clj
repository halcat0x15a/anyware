(ns felis.test.string
  (:refer-clojure :exclude [not-empty])
  (:require [clojure.data.generators :as gen]
            [clojure.test.generative :refer :all]
            [felis.test :as test]
            [felis.string :as string]))

(defspec not-empty
  string/split-lines
  [^string _]
  (is (not (empty? %))))

(defspec not-nil
  #(%1 %2)
  [^{:tag (gen/rand-nth [string/rest string/butlast])} function
   ^string string]
  (is (not (nil? %))))

(defspec not-contains-lt-and-rl
  (comp set string/escape)
  [^string string]
  (is (and (not (contains? \< %))
           (not (contains? \> %)))))
