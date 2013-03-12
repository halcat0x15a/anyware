(ns anyware.core.language)

(def filename #"\.(\w+)$")

(defmulti extension (fn [name] (->> name (re-find filename) first)))
(defmethod extension :default [_] identity)
