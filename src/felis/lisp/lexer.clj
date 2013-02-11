(ns felis.lisp.lexer
  (:refer-clojure :exclude [int keyword list])
  (:require [clojure.core :as core]
            [clojure.string :as string]
            [felis.parser :as parser]))

(declare lisp)

(def nil' (parser/parser #"^nil" (constantly nil)))

(def true' (parser/parser #"^true" (constantly true)))

(def false' (parser/parser #"^false" (constantly false)))

(def int
  (parser/parser
   #"^\d+"
   (fn [number]
     (reduce (fn [m n] (+ (* m 10) n))
             (map #(- (core/int %) (core/int \0)) number)))))

(def string (parser/parser #"^\".*\""))

(def keyword (parser/parser #"^:.*" core/keyword))

(def identifier (parser/parser #"^\S+" symbol))

(def list
  (parser/parser
   #"^\((.*)\)"
   (fn [[_ list]]
     (map (partial parser/parse' lisp)
          (string/split list #"\s+")))))

(def lisp
  (parser/repeat (parser/or list string keyword int nil' true' false' identifier)))
