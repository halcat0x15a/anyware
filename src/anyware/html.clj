(ns anyware.html
  (:require [clojure.string :as string]))

(defprotocol Node
  (render [node])
  (length [node])
  (split [node n]))

(extend-protocol Node
  String
  (render [node] node)
  (length [node] (count node))
  (split [node n] [(subs node 0 n) (subs node n)])
  clojure.lang.Sequential
  (render [node] (string/join (map render node)))
  (length [node] (reduce (fn [n node] (+ n (length node))) node))
  (split [node n]
    (loop [node node left []]
      (let [head (first node)]
        (if (< (length head) n)
          (recur (next node) (conj left head))
          (let [[left' right] (split head n)] [(conj left left') (cons right (next node))]))))))

(defrecord Elem [tag attrs node]
  Node
  (render [elem]
    (let [attrs (reduce (fn [s [k v]] (str s \space k \= \" v \")) "" attrs)]
      (str \< tag attrs \> (render node) \< \/ tag \>)))
  (length [elem]
    (length node))
  (split [elem n]
    (let [[left right] (split node n)]
      [(Elem. tag attrs left) (Elem. tag attrs right)])))

(defn elem [tag attrs node]
  (let [node (if (char? node) (str node) node)]
    (Elem. tag attrs node)))
