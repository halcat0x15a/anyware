(ns felis.language
  (:require [felis.parser :as parser]))

(def text (parser/regex #"[\s\S]*"))

(defmulti extension
  (let [extension #"\.(\w+)$"]
    (fn [name] (re-find extension name))))

(defmethod extension :default [_] text)

(defn highlight [parser source]
  (-> source parser :result))
