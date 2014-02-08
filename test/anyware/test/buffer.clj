(ns anyware.test.buffer
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [anyware.core.buffer :as buffer]))

(defn buffer []
  (buffer/->Buffer (gen/string) (gen/string)))

(defn field []
  (gen/rand-nth [:left :right]))

(defn regex []
  (gen/rand-nth [buffer/character buffer/line buffer/word]))

(defspec double-complement
  (comp buffer/complement buffer/complement)
  [^{:tag `field} field]
  (assert (= % field)))

(defspec preserving-move
  buffer/move
  [^{:tag `buffer} buffer ^{:tag `field} field ^{:tag `regex} regex]
  (assert (= (str %) (str buffer))))
