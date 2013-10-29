
(defmulti render
  (fn
    ([node] ::render)
    ([{:keys [name value] :as node} cursor]
       (cond (string? node) ::string
             (vector? node) ::vector
             (and name value) ::label))))

(defmethod render ::render [node]
  (render node 0))
(defmethod render ::string [node cursor]
  (+ cursor (count node)))
(defmethod render ::label [{:keys [name value]} cursor]
  (let [n (render value cursor)]
    (dotimes [i (- n cursor)]
      (aset *display* (+ cursor i) (get *style* name)))
    n))
(defmethod render ::vector [node cursor]
  (reduce #(render %2 %1) cursor node))

(def ^:dynamic *style*
  {:cursor (Color. "white" "black")
   :symbol (Color. "blue" "white")
   :string (Color. "maroon" "white")
   :keyword (Color. "aqua" "white")
   :special (Color. "magenta" "white")
   :default (Color. "black" "white")})

        {:keys [value next]} (parser/parse clojure/expression (buffer/show current))
    (loop [[node & tree] (flatten value)
           n 0]
      (when tree
        (cond (string? node) (recur tree (+ n (count node)))
              (:name node) (let [color (*style* (:name node))
                                 size (count (:value node))]
                             (dotimes [i size]
                               (aset display (+ n i) color))
                             (recur tree (+ n size))))))
