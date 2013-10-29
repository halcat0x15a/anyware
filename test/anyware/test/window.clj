(ns anyware.test.window
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [anyware.core.window :as window]))

(defn window []
  (gen/map gen/string (constantly {})))
