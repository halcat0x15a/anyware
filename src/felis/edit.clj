(ns felis.edit
  (:refer-clojure :exclude [remove]))

(defmulti invert identity)

(defmulti add (fn [_ field _] field))

(defmulti remove (fn [_ field] field))

(defmulti head (fn [_ field] field))

(defn move [edit field]
  (if-let [value (head edit field)]
    (-> edit
        (add (invert field) value)
        (remove field))
    edit))

(defn end [edit field]
  (let [edit' (move edit field)]
    (if (identical? edit edit')
      edit
      (recur edit' field))))
