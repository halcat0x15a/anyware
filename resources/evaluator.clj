(let [eval-function (atom nil)
      primitive-procedures (list (list (quote car) first)
                                 (list (quote cdr) next)
                                 (list (quote cons) cons)
                                 (list (quote nil?) nil?)
                                 (list (quote -) -)
                                 (list (quote *) *)
                                 (list (quote =) =))
      the-empty-environment (list)]
  (letfn [(map [f xs]
            (if (nil? xs)
              xs
              (cons (f (first xs)) (map f (next xs)))))
          (eval [exp env]
            ((deref eval-function) exp env))
          (self-evaluating? [exp]
            (cond (number? exp) true
                  (string? exp) true
                  :else false))
          (tagged-list? [exp tag]
            (= (first exp) tag))
          (variable? [exp] (symbol? exp))
          (quoted? [exp]
            (tagged-list? exp (quote quote)))
          (text-of-quotation [exp] (fnext exp))
          (lambda? [exp]
            (tagged-list? exp (quote lambda)))
          (lambda-parameters [exp]
            (fnext exp))
          (lambda-body [exp] (nnext exp))
          (make-lambda [parameters body]
            (cons (quote lambda) (cons parameters body)))
          (assignment? [exp]
            (tagged-list? exp (quote set!)))
          (assignment-variable [exp] (fnext exp))
          (assignment-value [exp] (fnext (next exp)))
          (definition? [exp]
            (tagged-list? exp (quote define)))
          (definition-variable [exp]
            (if (symbol? (fnext exp))
              (fnext exp)
              (first (fnext exp))))
          (definition-value [exp]
            (if (symbol? (fnext exp))
              (fnext (next exp))
              (make-lambda (next (fnext exp))
                           (nnext exp))))
          (if? [exp] (tagged-list? exp (quote if)))
          (if-predicate [exp] (fnext exp))
          (if-consequent [exp] (fnext (next exp)))
          (if-alternative [exp]
            (if (not (nil? (next (nnext exp))))
              (fnext (nnext exp))
              false))
          (make-if [predicate consequent alternative]
            (list (quote if) predicate consequent alternative))
          (begin? [exp] (tagged-list? exp (quote begin)))
          (begin-actions [exp] (next exp))
          (last-exp? [seq] (nil? (next seq)))
          (first-exp [seq] (first seq))
          (rest-exps [seq] (next seq))
          (sequence->exp [seq]
            (cond (nil? seq) seq
                  (last-exp? seq) (first-exp seq)
                  :else (make-begin seq)))
          (make-begin [seq]
            (cons (quote begin) seq))
          (application? [exp] (not (nil? exp)))
          (operator [exp] (first exp))
          (operands [exp] (next exp))
          (no-operands? [ops] (nil? ops))
          (first-operand [ops] (first ops))
          (rest-operands [ops] (next ops))
          (cond? [exp] (tagged-list? exp (quote cond)))
          (cond-clauses [exp] (next exp))
          (cond-else-clause? [clause]
            (= (cond-predicate clause) (quote else)))
          (cond-predicate [clause] (first clause))
          (cond-actions [clause] (next clause))
          (cond->if [exp]
            (expand-clauses (cond-clauses exp)))
          (expand-clauses [clauses]
            (if (nil? clauses)
              false
              (let [first (first clauses)
                    rest (next clauses)]
                (if (cond-else-clause? first)
                  (if (nil? rest)
                    (sequence->exp (cond-actions first))
                    (error "ELSE clause isn't last -- COND->IF"
                           clauses))
                  (make-if (cond-predicate first)
                           (sequence->exp (cond-actions first))
                           (expand-clauses rest))))))
          (true? [x]
            (not (= x false)))
          (false? [x]
            (= x false))
          (make-procedure [parameters body env]
            (list (quote procedure) parameters body env))
          (compound-procedure? [p]
            (tagged-list? p (quote procedure)))
          (procedure-parameters [p] (fnext p))
          (procedure-body [p] (fnext (next p)))
          (procedure-environment [p] (fnext (nnext p)))
          (enclosing-environment [env] (next env))
          (list-of-values [exps env]
            (if (no-operands? exps)
              (list)
              (cons (eval (first-operand exps) env)
                    (list-of-values (rest-operands exps) env))))
          (eval-if [exp env]
            (if (true? (eval (if-predicate exp) env))
              (eval (if-consequent exp) env)
              (eval (if-alternative exp) env)))
          (eval-sequence [exps env]
            (cond (last-exp? exps) (eval (first-exp exps) env)
                  :else
                  (do
                    (eval (first-exp exps) env)
                    (eval-sequence (rest-exps exps) env))))
          (first-frame [env] (first env))
          (make-frame [variables values]
            (atom (cons variables (map atom values))))
          (frame-variables [frame] (first (deref frame)))
          (frame-values [frame] (next (deref frame)))
          (set-variable-value! [var val env]
            (letfn [(env-loop [env]
                      (letfn [(scan [vars vals]
                                (cond (nil? vars)
                                      (env-loop
                                       (enclosing-environment env))
                                      (= var (first vars))
                                      (reset! (first vals) val)
                                      :else
                                      (scan (next vars) (next vals))))]
                        (if (= env the-empty-environment)
                          (error "Unbound variable -- SET!" var)
                          (let [frame (first-frame env)]
                            (scan (frame-variables frame)
                                  (frame-values frame))))))]
              (env-loop env)))
          (eval-assignment [exp env]
            (do
              (set-variable-value! (assignment-variable exp)
                                   (eval (assignment-value exp) env)
                                   env)
              (quote ok)))
          (add-binding-to-frame! [var val frame]
            (reset! frame (cons (cons var (first (deref frame)))
                      (cons (atom val) (next (deref frame))))))
          (define-variable! [var val env]
            (let [frame (first-frame env)]
              (letfn [(scan [vars vals]
                        (cond (nil? vars)
                              (add-binding-to-frame! var val frame)
                              (= var (first vars))
                              (reset! (first vals) val)
                              :else (scan (next vars) (next vals))))]
                (scan (frame-variables frame)
                      (frame-values frame)))))
          (eval-definition [exp env]
            (do
              (define-variable! (definition-variable exp)
                (eval (definition-value exp) env)
                env)
              (quote ok)))
          (evaluator-apply [procedure arguments]
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
                  (error "Unknown procedure type -- APPLY" procedure)))
          (extend-environment [vars vals base-env]
            (if (= (count vars) (count vals))
              (cons (make-frame vars vals) base-env)
              (if (< (count vars) (count vals))
                (error "Too many arguments supplied" vars vals)
                (error "Too few arguments supplied" vars vals))))
          (lookup-variable-value [var env]
            (letfn [(env-loop [env]
                      (letfn [(scan [vars vals]
                                (cond (nil? vars)
                                      (env-loop
                                       (enclosing-environment env))
                                      (= var (first vars))
                                      (deref (first vals))
                                      :else
                                      (scan (next vars) (next vals))))]
                              (if (= env the-empty-environment)
                                (error "Unbound variable" var)
                                (let [frame (first-frame env)]
                                  (scan (frame-variables frame)
                                        (frame-values frame))))))]
              (env-loop env)))
          (primitive-procedure-names []
            (map first primitive-procedures))
          (primitive-procedure-objects []
            (map (fn [proc] (list (quote primitive) (fnext proc)))
                 primitive-procedures))
          (setup-environment []
            (let [initial-env
                  (extend-environment (primitive-procedure-names)
                                      (primitive-procedure-objects)
                                      the-empty-environment)]
              (do
                (define-variable! (quote true) true initial-env)
                (define-variable! (quote false) false initial-env)
                initial-env)))
          (primitive-procedure? [proc]
            (tagged-list? proc (quote primitive)))
          (primitive-implementation [proc] (fnext proc))
          (apply-primitive-procedure [proc args]
            (apply
             (primitive-implementation proc) args))]
    (do
      (reset!
       eval-function
       (fn [exp env]
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
               :else (error "Unknown expression type -- EVAL" exp))))
      (fn [exp] (eval exp (setup-environment))))))
