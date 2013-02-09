(ns felis.lisp
  (:refer-clojure :exclude [eval apply])
  (:require [clojure.core :as core]))

(defrecord Lambda [environment parameters body])

(defn literal? [exp]
  (or (true? exp)
      (false? exp)
      (number? exp)
      (string? exp)
      (keyword? exp)
      (nil? exp)))

(defmulti eval-form (fn [_ [tag]] tag))

(defn eval
  ([exp] (eval (atom {}) exp))
  ([env exp]
     (cond (literal? exp) exp
           (symbol? exp) (get @env exp)
           (list? exp) (eval-form env exp))))

(defmulti apply (fn [procedure _] (type procedure)))

(defmethod apply Lambda [{:keys [environment parameters body]} arguments]
  (eval (merge environment (interleave parameters arguments)) body))

(defmethod apply :default [procedure arguments]
  (core/apply procedure arguments))

(defmethod eval-form 'quote [env [_ & quotation]] quotation)

(defmethod eval-form 'def [env [_ variable value]]
  (swap! env assoc variable (eval env value)))

(defmethod eval-form 'if [env [_ predicate consequent alternative]]
  (if (eval env predicate)
    (eval env consequent)
    (eval env alternative)))

(defmethod eval-form 'fn [env [_ parameters body]]
  (Lambda. env parameters body))

(defmethod eval-form 'do
  [env [_ & exps]]
  (if (not-empty exps)
    (loop [[exp & exps] exps]
      (let [value (eval env exp)]
        (if (empty? exps)
          value
          (recur exps))))))

(defn cond->if [[predicate value & clauses]]
  (list 'if predicate value
        (if (not-empty clauses)
          (cond->if clauses))))

(defmethod eval-form 'cond [env [_ & clauses]]
  (eval env (cond->if clauses)))

(defn let->fn [[variable value & bindings' :as bindings] body]
  (if (empty? bindings)
    (list (list 'fn [] body))
    (list (list 'fn [variable]
                (if (empty? bindings')
                  body
                  (let->fn bindings' body)))
          value)))

(defmethod eval-form 'let [env [_ bindings body]]
  (eval env (let->fn bindings body)))

(defmethod eval-form :default
  [env [operator & operands]]
  (apply (eval env operator)
         (map (partial eval env) operands)))
