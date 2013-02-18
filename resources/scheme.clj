(defn null? [x]
  (or (nil? x)
      (not (and (seq? x) (not (empty? x))))))

(defn append [list1 list2]
  (if (null? list1)
    list2
    (cons (first list1) (append (rest list1) list2))))

(defn assoc [key records]
  (cond (null? records) false
        (= key (ffirst records)) (first records)
        :else (assoc key (rest records))))

(defn for-each [proc list]
  (if (null? list)
    nil
    (do
      (proc (first list))
      (for-each proc (rest list)))))

(defn map [proc list]
  (if (null? list)
    list
    (cons (proc (first list))
          (map proc (rest list)))))

(defn memq [item x]
  (cond (null? x) false
        (= item (first x)) x
        :else (memq item (rest x))))
