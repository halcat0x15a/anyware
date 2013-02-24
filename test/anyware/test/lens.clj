(ns anyware.test.lens
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.lens :as lens]))

(defn lens []
  (gen/rand-nth [lens/name
                 lens/history
                 lens/buffer
                 lens/minibuffer
                 lens/mode]))

(defspec get-lens
  lens/get
  [^{:tag `lens} lens ^test/editor editor]
  (is (not (nil? %))))
