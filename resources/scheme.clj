(defn null? [x]
  (or (nil? x)
      (not (and (seq? x) (not (empty? x))))))

(defn append [list1 list2]
  (if (null? list1)
    list2
    (cons (first list1) (append (rest list1) list2))))


(defn map [proc list]
  (if (null? list)
    list
    (cons (proc (first list))
          (map proc (rest list)))))

(defn memq [item x]
  (cond (null? x) false
        (= item (first x)) x
        :else (memq item (rest x))))
