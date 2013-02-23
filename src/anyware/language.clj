(ns anyware.language
  (:require [anyware.parser :as parser]
            [anyware.language.clojure :as clojure]))

(def text (parser/regex #"[\s\S]*"))

(defmulti extension
  (let [extension #"\.(\w+)$"]
    (comp second (partial re-find extension))))

(defmethod extension "clj" [_] clojure/expressions)

(defmethod extension :default [_] text)
