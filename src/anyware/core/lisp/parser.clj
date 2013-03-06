(ns anyware.core.lisp.parser
  (:refer-clojure :exclude [constantly eval])
  (:require [anyware.core.lisp.environment :as environment]
            [anyware.core.lisp.derived :as derived]))

(defrecord Failure [message object])

(defmulti analyze-form (fn [[tag]] tag))

(defn literal? [exp]
  (or (true? exp)
      (false? exp)
      (number? exp)
      (string? exp)
      (keyword? exp)
      (nil? exp)))

(defn- constantly [value]
  (fn [_ k] (k value)))

(defn- lookup [variable]
  (fn [env k]
    (if (contains? env variable)
      (k (get env variable))
      (Failure. "Unable to resolve symbol" variable))))

(defn analyze [exp]
  (cond (literal? exp) (constantly exp)
        (symbol? exp) (lookup exp)
        (seq? exp) (analyze-form exp)
        :else (Failure. "Unknown expression type" exp)))

(defn eval
  ([exp] (eval environment/global exp))
  ([env exp] ((analyze exp) env identity)))

(defprotocol Function
  (call [function arguments k]))

(deftype Lambda [environment name parameters body]
  Function
  (call [lambda arguments k]
    (body (assoc (merge environment (zipmap parameters arguments))
            name lambda)
          k)))

(defmethod analyze-form 'quote [[_ quotation]]
  (constantly quotation))

(defn- eval-if [predicate consequent alternative]
  (fn [env k]
    (predicate env
     (fn [pred]
       (if pred
         (consequent env k)
         (alternative env k))))))

(defmethod analyze-form 'if [[_ predicate consequent alternative]]
  (eval-if (analyze predicate)
           (analyze consequent)
           (analyze alternative)))

(defn- make-procedure
  ([parameters body] (make-procedure '*anonymous* parameters body))
  ([name parameters body]
     (fn [env k]
       (k (Lambda. env name parameters body)))))

(defmethod analyze-form 'fn [[_ parameters body]]
  (make-procedure parameters (analyze body)))

(defmethod analyze-form 'fn* [[_ name parameters body]]
  (make-procedure name parameters (analyze body)))

(defn- sequentially [f g]
  (fn [env k]
    (f env (fn [_] (g env k)))))

(defn- eval-sequence [proc [proc' & procs' :as procs]]
  (if proc'
    (recur (sequentially proc proc') procs')
    proc))

(defmethod analyze-form 'do [[_ exp & exps]]
  (eval-sequence (analyze exp) (map analyze exps)))

(defmethod analyze-form 'cond [[_ & clauses]]
  (-> clauses derived/cond->if analyze))

(defmethod analyze-form 'let [[_ bindings body]]
  (analyze (derived/let->fn bindings body)))

(defmethod analyze-form 'letfn [[_ bindings body]]
  (analyze (derived/letfn->let bindings body)))

(defmethod analyze-form 'and [[_ & predicates]]
  (-> predicates derived/and->if analyze))

(defmethod analyze-form 'or [[_ & predicates]]
  (-> predicates derived/or->if analyze))

(defn- eval-error [message object]
  (fn [env k]
    (message env
     (fn [message]
       (object env
        (fn [object]
          (Failure. message object)))))))

(defmethod analyze-form 'error [[_ message object]]
  (eval-error (analyze message) (analyze object)))

(defn appl [procedure arguments]
  (fn [env k]
    (procedure env
     (fn [operator]
       (arguments env
        (fn [operands]
          (if (fn? operator)
            (k (apply operator operands))
            (call operator operands k))))))))

(defmethod analyze-form 'apply [[_ operator operands]]
  (appl (analyze operator) (analyze operands)))

(defn- construct [args [proc & procs] env k]
  (if proc
    (proc env (fn [value] (construct (conj args value) procs env k)))
    (k args)))

(def eval-operands (partial partial construct []))

(defmethod analyze-form :default [[operator & operands]]
  (appl (analyze operator) (->> operands (map analyze) eval-operands)))
