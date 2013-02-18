(ns felis.syntax
  (:require [clojure.string :as string]
            [felis.parser :as parser]))

(defn highlight [parser source]
  (-> source parser :result))

(def default (parser/regex #".*"))
