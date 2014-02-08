(ns anyware.test.html
  (:require [clojure.test.generative :refer (defspec)]
            [clojure.data.generators :as gen]
            [clojure.xml :as xml])
  (:import [java.io ByteArrayInputStream]))

(comment

(defspec espace-string
  html/escape
  [^string string]
  (is (nil? (some #{\< \>} %))))

(defspec valid-html
  html/render
  [^test/editor editor]
  (is (->> (.getBytes ^String %) ByteArrayInputStream. xml/parse)))

)

