(ns anyware.test.buffer
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [anyware.core.buffer :as buffer]))

(defn viewer []
  (let [buffer (gen/string)
        size (count buffer)]
    (buffer/->Viewer buffer (if (zero? size) 0 (gen/uniform 0 size)))))

(defn editor []
  (buffer/->Editor (gen/string) (gen/string)))

(defn buffer []
  (gen/one-of viewer editor))

(defn field []
  (gen/rand-nth [:left :right]))

(defn regex []
  (gen/rand-nth [buffer/line buffer/word]))

(defspec double-complement
  (comp buffer/complement buffer/complement)
  [^{:tag `field} field]
  (assert (= % field)))

(defspec preserving-move
  buffer/move
  [^{:tag `buffer} buffer ^{:tag `field} field ^{:tag `regex} regex]
  (assert (= (buffer/show %) (buffer/show buffer))))
