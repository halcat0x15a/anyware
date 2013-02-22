(ns felis.language
  (:require [felis.parser :as parser]
            [felis.language.clojure :as clojure]))

(def text (parser/regex #"[\s\S]*"))

(defmulti extension
  (let [extension #"\.(\w+)$"]
    (comp second (partial re-find extension))))

(defmethod extension "clj" [_] clojure/expressions)

(defmethod extension :default [_] text)
