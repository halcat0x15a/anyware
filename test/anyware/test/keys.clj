(ns anyware.test.keys
  (:require [clojure.test.generative :refer [defspec is]]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.keys :refer :all]))

(defn path [] (gen/rand-nth all))

(defspec get-in-path
  get-in
  [^test/editor editor ^{:tag `path} path]
  (is (not (nil? %))))
