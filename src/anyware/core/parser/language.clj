(ns anyware.core.parser.language)

(def path #"\.(\w+)$")

(defmulti extension (fn [name] (->> name (re-find path) first)))
(defmethod extension :default [_] nil)
