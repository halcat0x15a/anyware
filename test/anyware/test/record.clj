(ns anyware.test.record
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [anyware.test :as test]
            [anyware.core.record :as record]))

(def lenses
  [:frame
   :minibuffer
   :mode
   record/window
   record/name
   record/history
   record/change
   record/buffer
   record/minibuffer])

(defn lens [] (gen/rand-nth lenses))

(defspec get-lens
  record/get
  [^{:tag `lens} lens ^test/editor editor]
  (is (not (nil? %))))

(defspec set-get
  (fn [editor lens value]
    (->> editor (record/set lens value) (record/get lens)))
  [^test/editor editor ^{:tag `lens} lens ^anything value]
  (is (= % value)))
