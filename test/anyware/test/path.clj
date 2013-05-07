(ns anyware.test.path
  (:require [clojure.test.generative :refer [defspec is]]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.path :refer :all]))

(defn path [] (gen/rand-nth paths))

(defspec get-in-path
  get-in
  [^test/editor editor ^{:tag `path} path]
  (is (not (nil? %))))
