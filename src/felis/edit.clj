(ns felis.edit
  (:refer-clojure :exclude [first]))

(defmulti invert identity)

(defmulti insert (fn [_ field _] field))

(defmulti delete (fn [field _] field))

(defmulti first (fn [field _] field))

(defn cursor [field edit]
  (-> edit field count))

(defn move [field edit]
  (if-let [value (first field edit)]
    (->> edit
         (insert value (invert field))
         (delete field))
    edit))

(defn end [field edit]
  (let [edit' (move field edit)]
    (if (identical? edit edit')
      edit
      (recur field edit'))))
