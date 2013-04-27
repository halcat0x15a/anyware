(ns anyware.test.html
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [clojure.xml :as xml]
            [anyware.test :as test]
            [anyware.core.api.format :as format]
            [anyware.core.api.format.html :as html])
  (:import [java.io ByteArrayInputStream]))

(defspec espace-string
  html/escape
  [^string string]
  (is (nil? (some #{\< \>} %))))

(defspec valid-html
  (partial format/render html/format)
  [^test/editor editor]
  (is (->> (.getBytes ^String %) ByteArrayInputStream. xml/parse)))
