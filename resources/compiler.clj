(defn compile [exp target linkage]
  (cond (self-evaluating? exp)
        (compile-self-evaluating exp target linkage)
        (quoted? exp) (compile-quoted exp target linkage)
        (variable? exp)
        (compile-variable exp target linkage)
        (assignment? exp)
        (compile-assignment exp target linkage)
        (definition? exp)
        (compile-definition exp target linkage)
        (if? exp) (compile-if exp target linkage)
        (lambda? exp) (compile-lambda exp target linkage)
        (begin? exp)
        (compile-sequence (begin-actions exp)
                          target
                          linkage)
        (cond? exp) (compile (cond->if exp) target linkage)
        (application? exp)
        (compile-application exp target linkage)
        :else (error "Unknown expression type -- COMPILE" exp)))

(defn make-instruction-sequence [needs modifies statements]
  (list needs modifies statements))

(defn empty-instruction-sequence []
  (make-instruction-sequence (list) (list) (list)))

(defn compile-linkage [linkage]
  (cond (= linkage (quote return))
        (make-instruction-sequence (quote (continue)) (list)
                                   (quote ((goto (reg continue)))))
        (= linkage (quote next)) (empty-instruction-sequence)
        :else
        (make-instruction-sequence (list) (list)
                                   (list (list (quote goto)
                                               (list (quote label) linkage))))))

(defn end-with-linkage [linkage instruction-sequence]
  (preserving (quote (continue))
              instruction-sequence
              (compile-linkage linkage)))

(defn compile-self-evaluating [exp target linkage]
  (end-with-linkage linkage
    (make-instruction-sequence (list) (list target)
                               (list (list (quote assign)
                                           target
                                           (list (quote const)
                                                 exp))))))

(defn compile-quoted [exp target linkage]
  (end-with-linkage linkage
    (make-instruction-sequence (list) (list target)
                               (list (list (quote assign)
                                           target
                                           (list (quote const)
                                                 (text-of-quotation exp)))))))


(defn compile-variable [exp target linkage]
  (end-with-linkage linkage
    (make-instruction-sequence (quote (env)) (list target)
                               (list (list (quote assign)
                                           target
                                           (quote (op lookup-variable-value))
                                           (list (quote const) exp)
                                           (quote (reg env)))))))

(defn compile-assignment [exp target linkage]
  (let [var (assignment-variable exp)
        get-value-code (compile (assignment-value exp)
                                (quote val)
                                (quote next))]
    (end-with-linkage linkage
      (preserving (quote (env))
                  get-value-code
                  (make-instruction-sequence (quote (env val)) (list target)
                                             (list (list (quote perform)
                                                         (quote (op set-variable-value!))
                                                         (list (quote const) var)
                                                         (quote (reg val))
                                                         (quote (reg env)))
                                                   (list (quote assign) target (quote (const ok)))))))))

(defn compile-definition [exp target linkage]
  (let [var (definition-variable exp)
        get-value-code (compile (definition-value exp)
                                (quote val)
                                (quote next))]
    (end-with-linkage linkage
      (preserving (quote (env))
                   get-value-code
                   (make-instruction-sequence (quote (env val)) (list target)
                                              (list (list (quote perform)
                                                          (quote (op define-variable!))
                                                          (list (quote const) var)
                                                          (quote (reg val))
                                                          (quote (reg env)))
                                                    (list (quote assign) target (quote (const ok)))))))))

(defn compile-if [exp target linkage]
  (let [t-branch (make-label (quote true-branch))
        f-branch (make-label (quote false-branch))
        alter-if (make-label (quote akter-if))]
    (let [consequent-linkage
          (if (= linkage (quote next))
            alter-if
            linkage)]
      (let [p-code (compile (if-predicate exp) (quote val) (quote next))
            c-code (compile (if-consequent exp) target consequent-linkage)
            a-code (compile (if-alternative exp) target linkage)]
        (preserving (quote (env continue))
                    p-code
                    (append-instruction-sequences
                     (list (make-instruction-sequence (quote (val)) (list)
                                                      (list (quote (test (op false?) (reg val)))
                                                            (list (quote branch)
                                                                  (list (quote label)
                                                                        f-branch))))
                           (parallel-instruction-sequences
                            (append-instruction-sequences (list t-branch c-code))
                            (append-instruction-sequences (list f-branch a-code)))
                           alter-if)))))))

(defn compile-sequence [seq target linkage]
  (if (last-exp? seq)
    (compile (first-exp seq) target linkage)
    (preserving (quote (env continue))
                (compile (first-exp seq) target (quote next))
                (compile-sequence (rest-exps seq) target linkage))))

(defn compile-lambda [exp target linkage]
  (let [proc-entry (make-label (quote entry))
        after-lambda (make-label (quote after-lambda))]
    (let [lambda-linkage
          (if (= linkage (quote next))
            after-lambda
            linkage)]
      (append-instruction-sequences
       (list (tack-on-instruction-sequence
              (end-with-linkage lambda-linkage
                (make-instruction-sequence (quote (env)) (list target)
                                           (list (list (quote assign) target
                                                       (quote (op make-compiled-procedure))
                                                       (list (quote label) proc-entry)
                                                       (quote (reg env))))))
              (compile-lambda-body exp proc-entry))
             after-lambda)))))

(defn compile-lambda-body [exp proc-entry]
  (let [formals (lambda-parameters exp)]
    (append-instruction-sequences
     (list (make-instruction-sequence (quote (env proc argl)) (quote (env))
                                      (list proc-entry
                                            (quote (assign env (op compiled-procedure-env) (reg proc)))
                                            (list (quote assign) (quote env)
                                                  (quote (op extend-environment))
                                                  (list (quote const) formals)
                                                  (quote (reg argl))
                                                  (quote (reg env)))))
           (compile-sequence (lambda-body exp) (quote val) (quote return))))))

(defn compile-application [exp target linkage]
  (let [proc-code (compile (operator exp) (quote proc) (quote next))
        operand-codes
        (map (fn [operand] (compile operand (quote val) (quote next)))
             (operands exp))]
    (preserving (quote (env continue))
                proc-code
                (preserving (quote (proc continue))
                            (construct-arglist operand-codes)
                            (compile-procedure-call target linkage)))))

(defn construct-arglist [operand-codes]
  (let [operand-codes (reverse operand-codes)]
    (if (null? operand-codes)
      (make-instruction-sequence (list) (quote (argl))
                                 (quote ((assign argl (const ())))))
      (let [code-to-get-last-arg
            (append-instruction-sequences
             (list (first operand-codes)
                   (make-instruction-sequence
                    (quote (val)) (quote (argl))
                    (quote ((assign argl (op list) (reg val)))))))]
        (if (null? (rest operand-codes))
          code-to-get-last-arg
          (preserving (quote (env))
                      code-to-get-last-arg
                      (code-to-get-rest-args
                       (rest operand-codes))))))))

(defn code-to-get-rest-args [operand-codes]
  (let [code-for-next-arg
        (preserving (quote (argl))
          (first operand-codes)
          (make-instruction-sequence
           (quote (val argl)) (quote (argl))
           (quote ((assign argl (op cons) (reg val) (reg argl))))))]
    (if (null? (rest operand-codes))
        code-for-next-arg
        (preserving (quote (env))
                    code-for-next-arg
                    (code-to-get-rest-args (rest operand-codes))))))

(defn compile-procedure-call [target linkage]
  (let [primitive-branch (make-label (quote primitive-branch))
        compiled-branch (make-label (quote compiled-branch))
        after-call (make-label (quote after-call))]
    (let [compiled-linkage
          (if (= linkage (quote next)) after-call linkage)]
      (append-instruction-sequences
       (list (make-instruction-sequence (quote (proc)) (list)
                                        (list (quote (test (op primitive-procedure?) (reg proc)))
                                              (list (quote branch) (list (quote label) primitive-branch))))
             (parallel-instruction-sequences
              (append-instruction-sequences
               (list compiled-branch
                     (compile-proc-appl target compiled-linkage)))
              (append-instruction-sequences
               (list primitive-branch
                     (end-with-linkage linkage
                       (make-instruction-sequence (quote (proc argl))
                                                  (list target)
                                                  (list (list (quote assign) target
                                                              (quote (op apply-primitive-procedure))
                                                              (quote (reg proc))
                                                              (quote (reg argl)))))))))
             after-call)))))

(defn compile-proc-appl [target linkage]
  (cond (and (= target (quote val)) (not (= linkage (quote return))))
        (make-instruction-sequence
         (quote (proc)) all-regs
         (list (list (quote assign) (quote continue)
                     (list (quote label)
                           linkage))
               (quote (assign val (op compiled-procedure-entry)
                              (reg proc)))
               (quote (goto (reg val)))))
        (and (not (= target (quote val)))
             (not (= linkage (quote return))))
        (let [proc-return (make-label (quote proc-return))]
          (make-instruction-sequence
           (quote (proc)) all-regs
           (list (list (quote assign) (quote continue)
                       (list (quote label) proc-return))
                 (quote (assign val (op compiled-procedure-entry)
                                (reg proc)))
                 (quote (goto (reg val)))
                 proc-return
                 (list (quote assign) target (quote (reg val)))
                 (list (quote goto) (list (quote label) linkage)))))
        (and (= target (quote val)) (= linkage (quote return)))
        (make-instruction-sequence (quote (proc continue)) all-regs
                                   (quote ((assign val (op compiled-procedure-entry)
                                                   (reg proc))
                                           (goto (reg val)))))
        (and (not (= target (quote val))) (= linkage (quote return)))
        (error "return linkage, target not val -- COMPILE"
               target)))

(defn registers-needed [s]
  (if (symbol? s) (list) (first s)))

(defn registers-modified [s]
  (if (symbol? s) (list) (fnext s)))

(defn statements [s]
  (if (symbol? s) (list s) (fnext (next s))))

(defn needs-register? [seq reg]
  (memq reg (registers-needed seq)))

(defn modifies-register? [seq reg]
  (memq reg (registers-modified seq)))

(defn append-instruction-sequences [seqs]
  (do
    (defn append-2-sequences [seq1 seq2]
      (make-instruction-sequence
       (list-union (registers-needed seq1)
                   (list-difference (registers-needed seq2)
                                    (registers-modified seq1)))
       (list-union (registers-modified seq1)
                   (registers-modified seq2))
       (append (statements seq1) (statements seq2))))
    (defn append-seq-list [seqs]
      (if (null? seqs)
        (empty-instruction-sequence)
        (append-2-sequences (first seqs)
                            (append-seq-list (rest seqs)))))
    (append-seq-list seqs)))

(defn list-union [s1 s2]
  (cond (null? s1) s2
        (memq (first s1) s2) (list-union (rest s1) s2)
        :else (cons (first s1) (list-union (rest s1) s2))))

(defn list-difference [s1 s2]
  (cond (null? s1) (list)
        (memq (first s1) s2) (list-difference (rest s1) s2)
        :else (cons (first s1)
                    (list-difference (rest s1) s2))))

(defn preserving [regs seq1 seq2]
  (if (null? regs)
    (append-instruction-sequences (list seq1 seq2))
    (let [first-reg (first regs)]
      (if (and (needs-register? seq2 first-reg)
               (modifies-register? seq1 first-reg))
        (preserving (rest regs)
                    (make-instruction-sequence
                     (list-union (list first-reg)
                                 (registers-needed seq1))
                     (list-difference (registers-modified seq1)
                                      (list first-reg))
                     (append
                      (append (list (list (quote save) first-reg))
                              (statements seq1))
                      (list (list (quote restore) first-reg))))
                    seq2)
        (preserving (rest regs) seq1 seq2)))))

(defn tack-on-instruction-sequence [seq body-seq]
  (make-instruction-sequence
   (registers-needed seq)
   (registers-modified seq)
   (append (statements seq) (statements body-seq))))

(defn parallel-instruction-sequences [seq1 seq2]
  (make-instruction-sequence
   (list-union (registers-needed seq1)
               (registers-needed seq2))
   (list-union (registers-modified seq1)
               (registers-modified seq2))
   (append (statements seq1) (statements seq2))))

(def label-counter (atom 0))

(defn new-label-number []
  (do
    (reset! label-counter (+ 1 (deref label-counter)))
    (deref label-counter)))

(defn make-label [name]
  (symbol (str name (new-label-number))))

(def all-regs (quote (env proc val argl continue)))

(defn make-compiled-procedure [entry env]
  (list (quote compiled-procedure) entry env))

(defn compiled-procedure? [proc]
  (tagged-list? proc (quote compiled-procedure)))

(defn compiled-procedure-entry [c-proc] (fnext c-proc))

(defn compiled-procedure-env [c-proc] (fnext (next c-proc)))

(compile
 (quote (define (factorial n)
          (if (= n 1)
            1
            (* (factorial (- n 1)) n))))
 (quote val)
 (quote next))
