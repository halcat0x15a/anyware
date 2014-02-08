(ns anyware.test.history
  (:require [clojure.data.generators :as gen]
            [clojure.test.generative :refer :all]
            [anyware.core.history :as history]))

(defn history []
  (history/history (gen/anything)))

(defspec undo
  (fn [history value]
    (-> history
        (history/commit value)
        history/undo))
  [^{:tag `history} history ^anything value]
  (assert (= (history/present %) (history/present history))))
