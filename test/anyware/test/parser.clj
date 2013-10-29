(ns anyware.test.parser
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.core.parser :as parser]))

(defn success []
  (parser/->Success (gen/string) (gen/string)))

(defn parser []
  (one-of gen/string))

(defspec literal-parser
  parser/parse
  [^{:tag `literal} literal ^string input]
  (is (if-let [value (:value %)]
        (= value literal)
        (= (:next %) input))))
