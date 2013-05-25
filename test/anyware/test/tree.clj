(ns anyware.test.tree
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [clojure.zip :as z]
            [anyware.core.tree :as tree]))

(defspec move-tree
  (fn [n s] (tree/move (tree/zip s) n))
  [^{:tag (uniform 0 Integer/MAX_VALUE)} n ^string s]
  (is (if (<= (count s) n)
        (z/end? %)
        (= (z/node %) (nth s n)))))

(defspec set-cursor
  (comp tree/cursor z/next tree/zip)
  [^string s]
  (is (:name (z/node %))))
