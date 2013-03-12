(ns anyware.core.format)

(defprotocol Node
  (render [node]))

(defn write [node]
  (cond (satisfies? Node node) (render node)
        (vector? node) (reduce str (mapv write node))
        :else (str node)))
