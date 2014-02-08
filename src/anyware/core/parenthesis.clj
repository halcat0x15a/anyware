(ns anyware.core.parenthesis
  (:require [anyware.core.parser :refer :all]))

(def open #{\( \[ \{})

(def close #{\) \] \}})

(defn parser [open close]
  (fn [input]
    (parse (chain vector open (many (choice (re-pattern (str "^[^" open close "]+")) (parenthesis open close))) close) input)))

(def parenthesis (parser "(" ")"))

(def bracket (parser "[" "]"))

(def brace (parser "{" "}"))
