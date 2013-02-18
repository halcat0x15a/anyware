(defn eval [exp env]
  (cond (self-evaluating? exp) exp
        (variable? exp) (lookup-variable-value exp env)
        (quoted? exp) (text-of-quotation exp)
        (assignment? exp) (eval-assignment exp env)
        (definition? exp) (eval-definition exp env)
        (if? exp) (eval-if exp env)
        (lambda? exp)
        (make-procedure (lambda-parameters exp)
                        (lambda-body exp)
                        env)
        (begin? exp)
        (eval-sequence (begin-actions exp) env)
        (cond? exp) (eval (cond->if exp) env)
        (application? exp)
        (evaluator-apply (eval (operator exp) env)
               (list-of-values (operands exp) env))
        :else
        (error "Unknown expression type -- EVAL" exp)))

(defn evaluator-apply [procedure arguments]
  (cond (primitive-procedure? procedure)
        (apply-primitive-procedure procedure arguments)
        (compound-procedure? procedure)
        (eval-sequence
         (procedure-body procedure)
         (extend-environment
          (procedure-parameters procedure)
          arguments
          (procedure-environment procedure)))
        :else
        (error
          "Unknown procedure type -- APPLY" procedure)))

(defn list-of-values [exps env]
  (if (no-operands? exps)
    (list)
    (cons (eval (first-operand exps) env)
          (list-of-values (rest-operands exps) env))))

(defn eval-if [exp env]
  (if (true? (eval (if-predicate exp) env))
    (eval (if-consequent exp) env)
    (eval (if-alternative exp) env)))

(defn eval-sequence [exps env]
  (cond (last-exp? exps) (eval (first-exp exps) env)
        :else
        (do
          (eval (first-exp exps) env)
          (eval-sequence (rest-exps exps) env))))

(defn eval-assignment [exp env]
  (do
    (set-variable-value! (assignment-variable exp)
                         (eval (assignment-value exp) env)
                         env)
    (quote ok)))

(defn eval-definition [exp env]
  (do
    (define-variable! (definition-variable exp)
      (eval (definition-value exp) env)
      env)
    (quote ok)))

(defn self-evaluating? [exp]
  (cond (number? exp) true
        (string? exp) true
        :else false))

(defn variable? [exp] (symbol? exp))

(defn quoted? [exp]
  (tagged-list? exp (quote quote)))

(defn text-of-quotation [exp] (fnext exp))

(defn tagged-list? [exp tag]
  (if (not (null? exp))
    (= (first exp) tag)
    false))

(defn assignment? [exp]
  (tagged-list? exp (quote set!)))

(defn assignment-variable [exp] (fnext exp))

(defn assignment-value [exp] (fnext (next exp)))

(defn definition? [exp]
  (tagged-list? exp (quote define)))

(defn definition-variable [exp]
  (if (symbol? (fnext exp))
    (fnext exp)
    (first (fnext exp))))

(defn definition-value [exp]
  (if (symbol? (fnext exp))
    (fnext (next exp))
    (make-lambda (next (fnext exp))
                 (nnext exp))))

(defn lambda? [exp]
  (tagged-list? exp (quote lambda)))

(defn lambda-parameters [exp]
  (fnext exp))

(defn lambda-body [exp] (nnext exp))

(defn make-lambda [parameters body]
  (cons (quote lambda) (cons parameters body)))

(defn if? [exp] (tagged-list? exp (quote if)))

(defn if-predicate [exp] (fnext exp))

(defn if-consequent [exp] (fnext (next exp)))

(defn if-alternative [exp]
  (if (not (null? (next (nnext exp))))
    (fnext (nnext exp))
    false))

(defn make-if [predicate consequent alternative]
  (list (quote if) predicate consequent alternative))

(defn begin? [exp] (tagged-list? exp (quote begin)))

(defn begin-actions [exp] (rest exp))

(defn last-exp? [seq] (null? (rest seq)))

(defn first-exp [seq] (first seq))

(defn rest-exps [seq] (rest seq))

(defn sequence->exp [seq]
  (cond (null? seq) seq
        (last-exp? seq) (first-exp seq)
        :else (make-begin seq)))

(defn make-begin [seq]
  (cons (quote begin) seq))

(defn application? [exp] (not (null? exp)))

(defn operator [exp] (first exp))

(defn operands [exp] (rest exp))

(defn no-operands? [ops] (null? ops))

(defn first-operand [ops] (first ops))

(defn rest-operands [ops] (rest ops))

(defn cond? [exp] (tagged-list? exp (quote cond)))
(defn cond-clauses [exp] (rest exp))
(defn cond-else-clause? [clause]
  (= (cond-predicate clause) (quote else)))
(defn cond-predicate [clause] (first clause))
(defn cond-actions [clause] (rest clause))
(defn cond->if [exp]
  (expand-clauses (cond-clauses exp)))

(defn expand-clauses [clauses]
  (if (null? clauses)
    false
    (let [first (first clauses)
          rest (rest clauses)]
      (if (cond-else-clause? first)
        (if (null? rest)
          (sequence->exp (cond-actions first))
          (error "ELSE clause isn't last -- COND->IF"
                 clauses))
        (make-if (cond-predicate first)
                 (sequence->exp (cond-actions first))
                 (expand-clauses rest))))))

(defn true? [x]
  (not (= x false)))

(defn false? [x]
  (= x false))

(defn make-procedure [parameters body env]
  (list (quote procedure) parameters body env))

(defn compound-procedure? [p]
  (tagged-list? p (quote procedure)))

(defn procedure-parameters [p] (fnext p))

(defn procedure-body [p] (fnext (next p)))

(defn procedure-environment [p] (fnext (nnext p)))

(defn enclosing-environment [env] (rest env))
(defn first-frame [env] (first env))
(def the-empty-environment (list))

(defn make-frame [variables values]
  (atom (cons variables (map atom values))))
(defn frame-variables [frame] (first (deref frame)))
(defn frame-values [frame] (rest (deref frame)))
(defn add-binding-to-frame! [var val frame]
  (reset! frame (cons (cons var (first (deref frame)))
                      (cons (atom val) (rest (deref frame))))))

(defn extend-environment [vars vals base-env]
  (if (= (count vars) (count vals))
    (cons (make-frame vars vals) base-env)
    (if (< (count vars) (count vals))
      (error "Too many arguments supplied" vars vals)
      (error "Too few arguments supplied" vars vals))))

(defn lookup-variable-value [var env]
  (do
    (defn env-loop [env]
      (do
        (defn scan [vars vals]
          (cond (null? vars)
                (env-loop (enclosing-environment env))
                (= var (first vars))
                (deref (first vals))
                :else (scan (rest vars) (rest vals))))
        (if (= env the-empty-environment)
          (error "Unbound variable" var)
          (let [frame (first-frame env)]
            (scan (frame-variables frame)
                  (frame-values frame))))))
    (env-loop env)))

(defn set-variable-value! [var val env]
  (do
    (defn env-loop [env]
      (do
        (defn scan [vars vals]
          (cond (null? vars)
                (env-loop (enclosing-environment env))
                (= var (first vars))
                (reset! (first vals) val)
                :else (scan (next vars) (next vals))))
        (if (= env the-empty-environment)
          (error "Unbound variable -- SET!" var)
          (let [frame (first-frame env)]
            (scan (frame-variables frame)
                  (frame-values frame))))))
    (env-loop env)))

(defn define-variable! [var val env]
  (let [frame (first-frame env)]
    (do
      (defn scan [vars vals]
        (cond (null? vars)
              (add-binding-to-frame! var val frame)
              (= var (first vars))
              (reset! (first vals) val)
              :else (scan (next vars) (next vals))))
      (scan (frame-variables frame)
            (frame-values frame)))))

(defn setup-environment []
  (let [initial-env
        (extend-environment (primitive-procedure-names)
                            (primitive-procedure-objects)
                            the-empty-environment)]
    (do
      (define-variable! (quote true) true initial-env)
      (define-variable! (quote false) false initial-env)
      initial-env)))

(defn primitive-procedure? [proc]
  (tagged-list? proc (quote primitive)))

(defn primitive-implementation [proc] (fnext proc))

(def primitive-procedures
  (list (list (quote car) first)
        (list (quote cdr) next)
        (list (quote cons) cons)
        (list (quote null?) null?)
        (list (quote -) -)
        (list (quote *) *)
        (list (quote =) =)))

(defn primitive-procedure-names []
  (map first primitive-procedures))

(defn primitive-procedure-objects []
  (map (fn [proc] (list (quote primitive) (fnext proc)))
       primitive-procedures))

(defn apply-primitive-procedure [proc args]
  (apply
   (primitive-implementation proc) args))

(def the-global-environment (setup-environment))

(eval
 (quote
  (define (append x y)
    (if (null? x)
      y
      (cons (car x)
            (append (cdr x) y)))))
 the-global-environment)

(eval
 (quote
  (append (quote (a b c)) (quote (d e f))))
 the-global-environment)

(eval
 (quote
  (begin
   (define (factorial n)
     (if (= n 1)
       1
       (* (factorial (- n 1)) n)))
   (factorial 3)))
 the-global-environment)
