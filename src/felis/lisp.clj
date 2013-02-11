(ns felis.lisp
  (:refer-clojure :exclude [eval apply read-string])
  (:require [clojure.core :as core]
            [felis.parser :as parser]
            [felis.lisp.lexer :as lexer]
            [felis.lisp.environment :as environment]))

(defrecord Lambda [environment parameters body])
  
(defrecord Failure [message])

(defn expression [env exp] exp)

(defn literal? [exp]
  (or (true? exp)
      (false? exp)
      (number? exp)
      (char? exp)
      (string? exp)
      (keyword? exp)
      (nil? exp)))

(defmulti analyze-form (fn [[tag]] tag))

(defn- lookup [variable]
  (fn [k env]
    (if (contains? env variable)
      (k env (get env variable))
      (Failure. (str "Unable to resolve symbol: "
                     variable
                     " in this context")))))

(defn analyze [exp]
  (cond (literal? exp) (fn [k env] (k env exp))
        (symbol? exp) (lookup exp)
        (seq? exp) (analyze-form exp)))

(defn eval
  ([exp] (eval environment/global exp))
  ([env exp]
     ((analyze exp) expression env)))

(defmulti apply (fn [env procedure _] (type procedure)))

(defmethod apply Lambda [environment {:keys [parameters body]} arguments]
  (body expression (merge environment (zipmap parameters arguments))))

(defmethod apply :default [environment procedure arguments]
  (core/apply procedure arguments))

(defmethod analyze-form 'quote [[_ & quotation]]
  (fn [k env] (k env quotation)))

(defmethod analyze-form 'def [[_ variable value]]
  (let [eval-value (analyze value)]
    (fn [k env]
      (let [value (eval-value expression env)]
        (k (assoc env variable value) value)))))

(defmethod analyze-form 'if [[_ predicate consequent alternative]]
  (let [eval-predicate (analyze predicate)
        eval-consequent (analyze consequent)
        eval-alternative (analyze alternative)]
    (fn [k env]
      (if (eval-predicate expression env)
        (k env (eval-consequent expression env))
        (k env (eval-alternative expression env))))))
  
(defmethod analyze-form 'fn [[_ parameters body]]
  (let [eval-body (analyze body)]
    (fn [k env]
      (k env (Lambda. env parameters eval-body)))))

(defmethod analyze-form 'do [[_ & exps]]
  (letfn [(sequentially [f g]
            (fn [k env]
              (f (fn [env value]
                   (g k env))
                 env)))
          (eval-sequence [proc [proc' & procs' :as procs]]
            (if (empty? procs)
              proc
              (recur (sequentially proc proc') procs')))]
    (let [[proc & procs] (map analyze exps)]
      (if (empty? exps)
        (constantly nil)
        (eval-sequence proc procs)))))

(defn cond->if [[predicate value & clauses]]
  `(if ~predicate ~value
       ~(if (not-empty clauses)
         (cond->if clauses))))

(defmethod analyze-form 'cond [[_ & clauses]]
  (-> clauses cond->if analyze))

(defn and->if [[predicate & predicates' :as predicates]]
  (if (empty? predicates)
    true
    `(if ~predicate ~(and->if predicates') false)))

(defmethod analyze-form 'and [[_ & predicates]]
  (-> predicates and->if analyze))

(defn or->if [[predicate & predicates' :as predicates]]
  (if (empty? predicates)
    false
    `(if ~predicate true ~(or->if predicates'))))

(defmethod analyze-form 'or [[_ & predicates]]
  (-> predicates or->if analyze))

(defn let->fn [[variable value & bindings' :as bindings] body]
  (if (empty? bindings)
    (list (list [] body))
    (list (list [variable]
                (if (empty? bindings')
                  body
                  (let->fn bindings' body)))
          value)))

(defmethod analyze-form 'let [[_ bindings body]]
  (analyze (let->fn bindings body)))

(defn defn->def [name parameters body]
  (list 'def name
        (list 'fn parameters body)))

(defmethod analyze-form 'defn [[_ name parameters body]]
  (analyze (defn->def name parameters body)))

(defmethod analyze-form :default [[operator & operands]]
  (let [operator-eval (analyze operator)
        operand-evals (map analyze operands)]
    (fn [k env]
      (k env (apply env (operator-eval expression env)
                    (map #(% expression env) operand-evals))))))

(defn read-string [string]
  (eval (parser/parse' lexer/lisp string)))
