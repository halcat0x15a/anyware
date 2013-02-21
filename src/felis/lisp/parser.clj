(ns felis.lisp.parser
  (:refer-clojure :exclude [eval])
  (:require [felis.lisp.environment :as environment]))

(deftype Lambda [environment parameters body])

(defrecord Failure [message object])

(def fail? (partial instance? Failure))

(defmulti maybe (fn [value function] (type value)))

(defmethod maybe Failure [failure function] failure)

(defmethod maybe :default [value function] (function value))

(defn literal? [exp]
  (or (true? exp)
      (false? exp)
      (number? exp)
      (string? exp)
      (keyword? exp)
      (nil? exp)))

(defmulti analyze-form (fn [[tag]] tag))

(defn- lookup [variable]
  (fn [env]
    (if (contains? @env variable)
      (get @env variable)
      (Failure. "Unable to resolve symbol" variable))))

(defn analyze [exp]
  (cond (literal? exp) (constantly exp)
        (symbol? exp) (lookup exp)
        (seq? exp) (analyze-form exp)
        :else (Failure. "Unknown expression type" exp)))

(defn eval
  ([exp] (eval (atom environment/global) exp))
  ([env exp]
     ((analyze exp) env)))

(defmulti apply' (fn [procedure arguments] (type procedure)))

(defmethod apply' Lambda [lambda arguments]
  ((.body lambda)
   (atom (merge @(.environment lambda)
                (zipmap (.parameters lambda) arguments)))))

(defmethod apply' Failure [failure arguments] failure)

(defmethod apply' :default [procedure arguments]
  (cond (fn? procedure) (apply procedure arguments)
        :else (Failure. "Unknown procedure type" procedure)))

(defmethod analyze-form 'quote [[_ quotation]]
  (constantly quotation))

(defmethod analyze-form 'def [[_ variable value]]
  (let [eval-value (analyze value)]
    (fn [env]
      (swap! env assoc variable (eval-value env)))))

(defmethod analyze-form 'if [[_ predicate consequent alternative]]
  (let [eval-predicate (analyze predicate)
        eval-consequent (analyze consequent)
        eval-alternative (analyze alternative)]
    (fn [env]
      (maybe (eval-predicate env)
             #(if %
                (eval-consequent env)
                (eval-alternative env))))))

(defmethod analyze-form 'fn [[_ parameters body]]
  (let [eval-body (analyze body)]
    (fn [env]
      (Lambda. env parameters eval-body))))

(defmethod analyze-form 'do [[_ & exps]]
  (letfn [(sequentially [f g]
            (fn [env]
              (maybe (f env)
                     (fn [_]
                       (g env)))))
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
    (list (list 'fn [] body))
    (list (list 'fn [variable]
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

(defmethod analyze-form 'comment [[_ & exps]]
  (fn [env] nil))

(defmethod analyze-form 'error [[_ message object]]
  (let [eval-message (analyze message)
        eval-object (analyze object)]
    (fn [env]
      (Failure. (eval-message env) (eval-object env)))))

(defn eval-apply [operator operands]
  (fn [env]
    (maybe (operator env)
           (fn [operator]
             (let [operands (operands env)]
               (maybe (first (filter fail? operands))
                      (fn [_]
                        (apply' operator operands))))))))

(defmethod analyze-form 'apply [[_ operator operands]]
  (let [operator-eval (analyze operator)
        operands-eval (analyze operands)]
    (eval-apply operator-eval operands-eval)))

(defmethod analyze-form :default [[operator & operands]]
  (let [operator-eval (analyze operator)
        operand-evals (map analyze operands)
        operands-eval (fn [env] (map #(% env) operand-evals))]
    (eval-apply operator-eval operands-eval)))
