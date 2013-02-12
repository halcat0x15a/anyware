(ns felis.test.html
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [clojure.xml :as xml]
            [felis.test :as test]
            [felis.html :as html]
            [felis.editor :as editor])
  (:import [java.io ByteArrayInputStream]))

(defn block []
  (gen/hash-map gen/keyword gen/keyword))

(defn css []
  (gen/hash-map gen/keyword block))

(defspec not-contains-lt-and-rl
  (comp set html/escape)
  [^string string]
  (is (and (not (contains? \< %))
           (not (contains? \> %)))))

(defspec css-is-string
  html/css
  [^{:tag `css} css]
  (is (string? %)))

(defspec valid-html
  editor/render
  [^test/editor editor]
  (is (-> % .getBytes ByteArrayInputStream. xml/parse)))
