(ns anyware.core.language
  (:require [anyware.core.parser :as parser]))

(def filename #"\.(\w+)$")

(defmulti extension (fn [name] (->> name (re-find filename) first)))
(defmethod extension :default [_] parser/id)
