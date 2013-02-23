(ns anyware.test.html
  (:require [clojure.test.generative :refer (defspec is)]
            [clojure.data.generators :as gen]
            [clojure.xml :as xml]
            [anyware.test :as test]
            [anyware.html :as html]
            [anyware.editor :as editor])
  (:import [java.io ByteArrayInputStream]))

(defn block []
  (gen/hash-map gen/keyword gen/string))

(defn css []
  (gen/hash-map gen/keyword block))

(declare element node-seq)

(defn node []
  ((gen/weighted {gen/string 2
                  element 1
                  node-seq 1})))

(defn element []
  (html/->Element (gen/keyword) (block) (node)))

(defn node-seq []
  (html/->NodeSeq (node) (gen/list node (dec (partial gen/geometric 0.2)))))

(defspec espace-string
  (comp set html/escape)
  [^string string]
  (is (and (not (contains? \< %))
           (not (contains? \> %))
           (not (contains? \& %)))))

(defspec css-is-string
  (comp html/write html/css)
  [^{:tag `css} css]
  (is (string? %)))

(defspec valid-html
  (comp html/write html/html)
  [^test/editor editor]
  (is (-> % .getBytes ByteArrayInputStream. xml/parse)))
