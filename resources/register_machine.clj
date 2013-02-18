(defn make-machine [register-names ops controller-text]
  (let [machine (make-new-machine)]
    (do
      (for-each (fn [register-name]
                  ((machine (quote allocate-register)) register-name))
                register-names)
      ((machine (quote install-operations)) ops)
      ((machine (quote install-instruction-sequence))
       (assemble controller-text machine))
      machine)))

(defn make-register [name]
  (let [contents (atom (quote *unassigned*))]
    (do
      (defn dispatch [message]
        (cond (= message (quote get)) (deref contents)
              (= message (quote set))
              (fn [value] (reset! contents value))
              :else
              (error "Unknown request -- REGISTER" message)))
      dispatch)))

(defn get-contents [register]
  (register (quote get)))

(defn set-contents! [register value]
  ((register (quote set)) value))

(defn make-stack []
  (let [s (atom (list))]
    (do
      (defn push [x]
        (reset! s (cons x (deref s))))
      (defn pop []
        (if (null? (deref s))
          (error "Empty stack -- POP" s)
          (let [top (first (deref s))]
            (do
              (reset! s (rest (deref s)))
              top))))
      (defn initialize []
        (do
          (reset! s (list))
          (quote done)))
      (defn dispatch [message]
        (cond (= message (quote push)) push
              (= message (quote pop)) (pop)
              (= message (quote initialize)) (initialize)
              :else (error "Unknown request -- STACK" message)))
      dispatch)))

(defn pop [stack]
  (stack (quote pop)))

(defn push [stack value]
  ((stack (quote push)) value))

(defn start [machine]
  (machine (quote start)))

(defn get-register-contents [machine register-name]
  (get-contents (get-register machine register-name)))

(defn set-register-contents! [machine register-name value]
  (do
    (set-contents! (get-register machine register-name) value)
    (quote done)))

(defn get-register [machine reg-name]
  ((machine (quote get-register)) reg-name))

(defn make-new-machine []
  (let [pc (make-register (quote pc))
        flag (make-register (quote flag))
        stack (make-stack)
        the-instruction-sequence (atom (list))]
    (let [the-ops (atom (list (list (quote initialize-stack)
                                    (fn [] (stack (quote initialize))))))
          register-table (atom (list (list (quote pc) pc)
                                     (list (quote flag) flag)))]
      (do
        (defn allocate-register [name]
          (do
            (if (assoc name (deref register-table))
              (error "Multiply defined register" name)
              (reset! register-table
                      (cons (list name (make-register name))
                            (deref register-table))))
            (quote register-allocated)))
        (defn lookup-register [name]
          (let [val (assoc name (deref register-table))]
            (if val
              (fnext val)
              (error "Unknown register" name))))
        (defn execute []
          (let [insts (get-contents pc)]
            (if (or (nil? insts) (null? insts))
              (quote done)
              (do
                ((instruction-execution-proc (first insts)))
                (execute)))))
        (defn dispatch [message]
          (cond (= message (quote start))
                (do
                  (set-contents! pc (deref the-instruction-sequence))
                  (execute))
                (= message (quote install-instruction-sequence))
                (fn [seq]
                  (reset! the-instruction-sequence seq))
                (= message (quote allocate-register)) allocate-register
                (= message (quote get-register)) lookup-register
                (= message (quote install-operations))
                (fn [ops] (reset! the-ops (append (deref the-ops) ops)))
                (= message (quote stack)) stack
                (= message (quote operations)) (deref the-ops)
                :else (error "Unknown request -- MACHINE" message)))
        dispatch))))

(defn assemble [controller-text machine]
  (extract-labels controller-text
                  (fn [insts labels]
                    (do
                      (update-insts! insts labels machine)
                      insts))))

(defn extract-labels [text receive]
  (if (null? text)
    (receive (list) (list))
    (extract-labels (rest text)
                    (fn [insts labels]
                      (let [next-inst (first text)]
                        (if (symbol? next-inst)
                          (receive insts
                                   (cons (make-label-entry next-inst
                                                           insts)
                                         labels))
                          (receive (cons (make-instruction next-inst)
                                         insts)
                                   labels)))))))

(defn update-insts! [insts labels machine]
  (let [pc (get-register machine (quote pc))
        flag (get-register machine (quote flag))
        stack (machine (quote stack))
        ops (machine (quote operations))]
    (for-each
     (fn [inst]
       (set-instruction-execution-proc!
        inst
        (make-execution-procedure
         (instruction-text inst) labels machine
         pc flag stack ops)))
     insts)))

(defn make-instruction [text]
  (cons text (list (atom nil))))

(defn instruction-text [inst]
  (first inst))

(defn instruction-execution-proc [inst]
  (deref (fnext inst)))

(defn set-instruction-execution-proc! [inst proc]
  (reset! (fnext inst) proc))

(defn make-label-entry [label-name insts]
  (atom (cons label-name insts)))

(defn lookup-label [labels label-name]
  (let [val (assoc label-name (map deref labels))]
    (if val
      (rest val)
      (error "Undefined label -- ASSEMBLE" label-name))))

(defn make-execution-procedure [inst labels machine pc flag stack ops]
  (cond (= (first inst) (quote assign))
        (make-assign inst machine labels ops pc)
        (= (first inst) (quote test))
        (make-test inst machine labels ops flag pc)
        (= (first inst) (quote branch))
        (make-branch inst machine labels flag pc)
        (= (first inst) (quote goto))
        (make-goto inst machine labels pc)
        (= (first inst) (quote save))
        (make-save inst machine stack pc)
        (= (first inst) (quote restore))
        (make-restore inst machine stack pc)
        (= (first inst) (quote perform))
        (make-perform inst machine labels ops pc)
        :else (error "Unknown instruction type -- ASSEMBLE"
                     inst)))

(defn make-assign [inst machine labels operations pc]
  (let [target
        (get-register machine (assign-reg-name inst))
        value-exp (assign-value-exp inst)]
    (let [value-proc
          (if (operation-exp? value-exp)
            (make-operation-exp
             value-exp machine labels operations)
            (make-primitive-exp
             (first value-exp) machine labels))]
      (fn []
        (do
          (set-contents! target (value-proc))
          (advance-pc pc))))))

(defn assign-reg-name [assign-instruction]
  (fnext assign-instruction))

(defn assign-value-exp [assign-instruction]
  (nnext assign-instruction))

(defn advance-pc [pc]
  (set-contents! pc (rest (get-contents pc))))

(defn make-test [inst machine labels operations flag pc]
  (let [condition (test-condition inst)]
    (if (operation-exp? condition)
      (let [condition-proc (make-operation-exp
                            condition machine labels operations)]
        (fn []
          (do
            (set-contents! flag (condition-proc))
            (advance-pc pc))))
      (error "Bad TEST instruction -- ASSEMBLE" inst))))

(defn test-condition [test-instruction]
  (rest test-instruction))

(defn make-branch [inst machine labels flag pc]
  (let [dest (branch-dest inst)]
    (if (label-exp? dest)
      (let [insts (lookup-label labels (label-exp-label dest))]
        (fn []
          (if (get-contents flag)
            (set-contents! pc insts)
            (advance-pc pc))))
      (error "Bad BRANCH instruction -- ASSEMBLE" inst))))

(defn branch-dest [branch-instruction]
  (fnext branch-instruction))

(defn make-goto [inst machine labels pc]
  (let [dest (goto-dest inst)]
    (cond (label-exp? dest)
          (let [insts (lookup-label labels
                                    (label-exp-label dest))]
            (fn [] (set-contents! pc insts)))
          (register-exp? dest)
          (let [reg (get-register machine
                                  (register-exp-reg dest))]
            (fn []
              (set-contents! pc (get-contents reg))))
          :else (error "Bad GOTO instruction -- ASSEMBLE"
                       inst))))

(defn goto-dest [goto-instruction]
  (fnext goto-instruction))

(defn make-save [inst machine stack pc]
  (let [reg (get-register machine
                          (stack-inst-reg-name inst))]
    (fn []
      (do
        (push stack (get-contents reg))
        (advance-pc pc)))))

(defn make-restore [inst machine stack pc]
  (let [reg (get-register machine
                          (stack-inst-reg-name inst))]
    (fn []
      (do
        (set-contents! reg (pop stack))
        (advance-pc pc)))))

(defn stack-inst-reg-name [stack-instruction]
  (fnext stack-instruction))

(defn make-perform [inst machine labels operations pc]
  (let [action (perform-action inst)]
    (if (operation-exp? action)
      (let [action-proc (make-operation-exp
                         action machine labels operations)]
        (fn []
          (do
            (action-proc)
            (advance-pc pc))))
      (error "Bad PERFORM instruction -- ASSEMBLE" inst))))

(defn perform-action [inst]
  (rest inst))

(defn make-primitive-exp [exp machine labels]
  (cond (constant-exp? exp)
        (let [c (constant-exp-value exp)]
          (fn [] c))
        (label-exp? exp)
        (let [insts (lookup-label labels
                                  (label-exp-label exp))]
          (fn [] insts))
        (register-exp? exp)
        (let [r (get-register machine
                              (register-exp-reg exp))]
          (fn [] (get-contents r)))
        :else (error "Unknown expression type -- ASSEMBLE" exp)))

(defn register-exp? [exp]
  (tagged-list? exp (quote reg)))

(defn register-exp-reg [exp]
  (fnext exp))

(defn constant-exp? [exp]
  (tagged-list? exp (quote const)))

(defn constant-exp-value [exp] (fnext exp))

(defn register-exp-reg [exp]
  (fnext exp))

(defn label-exp? [exp]
  (tagged-list? exp (quote label)))

(defn label-exp-label [exp]
  (fnext exp))

(defn make-operation-exp [exp machine labels operations]
  (let [op (lookup-prim (operation-exp-op exp) operations)
        aprocs
        (map (fn [e]
               (make-primitive-exp e machine labels))
             (operation-exp-operands exp))]
    (fn []
      (apply op (map (fn [p] (p)) aprocs)))))

(defn operation-exp? [exp]
  (and (not (null? exp)) (tagged-list? (first exp) (quote op))))

(defn operation-exp-op [operation-exp]
  (fnext (first operation-exp)))

(defn operation-exp-operands [operation-exp]
  (rest operation-exp))

(defn lookup-prim [symbol operations]
  (let [val (assoc symbol operations)]
    (if val
      (fnext val)
      (error "Unknown opeartion -- ASSEMBLE" symbol))))

(def gcd-machine
  (make-machine
   (quote (a b t))
   (list (list (quote mod) mod) (list (quote =) =))
   (quote
    (test-b
     (test (op =) (reg b) (const 0))
     (branch (label gcd-done))
     (assign t (op mod) (reg a) (reg b))
     (assign a (reg b))
     (assign b (reg t))
     (goto (label test-b))
     gcd-done))))

(set-register-contents! gcd-machine (quote a) 206)

(set-register-contents! gcd-machine (quote b) 40)

(start gcd-machine)

(get-register-contents gcd-machine (quote a))
