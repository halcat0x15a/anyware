(ns anyware.test.lens
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.lens :as lens]))

(defn lens []
  (gen/rand-nth [lens/name
                 lens/history
                 lens/buffers
                 lens/buffer
                 lens/minibuffer
                 lens/mode]))

(defspec get-lens
  lens/get
  [^{:tag `lens} lens ^test/editor editor]
  (is (not (nil? %))))

(defspec set-get
  (fn [editor lens value]
    (->> editor
         (lens/set lens value)
         (lens/get lens)))
  [^test/editor editor ^{:tag `lens} lens ^anything value]
  (is (= % value)))
