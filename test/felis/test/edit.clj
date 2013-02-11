(ns felis.test.edit
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [felis.test :as test]
            [felis.edit :as edit]))

(defn edit []
  ((gen/rand-nth [test/text test/buffer])))

(defn field []
  (gen/rand-nth [:lefts :rights]))

(defspec opposite-opposite
  (comp edit/opposite edit/opposite)
  [^{:tag `field} field]
  (is (= % field)))

(defspec insert-move-move-delete
  (fn [edit field]
    (-> edit
        (edit/insert field (test/element edit))
        (edit/move field)
        (edit/move (edit/opposite field))
        (edit/delete field)))
  [^{:tag `edit} edit ^{:tag `field} field]
  (is (= % edit)))
