(ns anyware.test.html
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [clojure.xml :as xml]
            [anyware.test :as test]
            [anyware.core.format :as format]
            [anyware.core.format.html :as html]
            [anyware.core.editor :as editor])
  (:import [java.io ByteArrayInputStream]))

(defspec espace-string
  html/escape
  [^string string]
  (is (nil? (some #{\< \>} %))))

(defspec valid-html
  (fn [editor]
    (-> editor format/render format/write))
  [^test/editor editor]
  (is (-> % .getBytes ByteArrayInputStream. xml/parse)))
