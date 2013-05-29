(ns anyware.core.language
  (:require [anyware.core.parser :as parser]
            [anyware.core.language.clojure :as clojure]))

(def filename #"\.(\w+)$")

(defmulti extension
  (fn [name] (some->> name (re-find filename) second)))
(defmethod extension "clj" [_] clojure/expressions)
(defmethod extension :default [_] parser/id)
(defmethod extension nil [_] parser/id)
