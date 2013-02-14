(ns felis.edit
  (:refer-clojure :exclude [remove]))

(defmulti invert identity)

(defmulti insert (fn [_ field _] field))

(defmulti delete (fn [field _] field))

(defmulti head (fn [field _] field))

(defn move [field edit]
  (if-let [value (head field edit)]
    (->> edit
         (insert value (invert field))
         (delete field))
    edit))

(defn end [field edit]
  (let [edit' (move field edit)]
    (if (identical? edit edit')
      edit
      (recur field edit'))))

(def left (partial move :lefts))

(def right (partial move :rights))

(defn append [char text]
  (insert char :lefts text))

(def backspace (partial delete :lefts))
