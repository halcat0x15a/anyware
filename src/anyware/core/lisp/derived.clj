(ns anyware.core.lisp.derived)

(defn cond->if [[predicate value & clauses]]
  `(if ~predicate ~value
       ~(if (not-empty clauses)
          (cond->if clauses))))

(defn and->if [[predicate & predicates' :as predicates]]
  (if (empty? predicates)
    true
    `(if ~predicate ~(and->if predicates') false)))

(defn or->if [[predicate & predicates' :as predicates]]
  (if (empty? predicates)
    false
    `(if ~predicate true ~(or->if predicates'))))

(defn let->fn [[parameter value & bindings] body]
  (if parameter
    (list (list 'fn [parameter] (let->fn bindings body)) value)
    body))

(defn letfn->let [bindings body]
  (list 'let
        (vec (interleave (map first bindings)
                         (map (partial cons 'fn*) bindings)))
        body))
(let->fn '[a 1 b 2] '(+ a b))
