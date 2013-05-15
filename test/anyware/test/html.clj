(ns anyware.test.html
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [clojure.xml :as xml]
            [anyware.test :as test]
            [anyware.core.html :as html])
  (:import [java.io ByteArrayInputStream]))

(defspec espace-string
  html/escape
  [^string string]
  (is (nil? (some #{\< \>} %))))

(defspec valid-html
  html/render
  [^test/editor editor]
  (is (->> (.getBytes ^String %) ByteArrayInputStream. xml/parse)))
