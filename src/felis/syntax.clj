(ns felis.syntax
  (:require [clojure.string :as string]
            [felis.parser :as parser]))

(defn highlight [parser source]
  (let [{:keys [source destination]} (parser/parse parser source)]
    [(persistent! destination) source]))

(def default (parser/parser #".*"))
