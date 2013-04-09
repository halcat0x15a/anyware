(ns anyware.core.function)

(defn safe [f]
  (fn [x] (if-let [y (f x)] y x)))

(defn combine
  ([f g] (partial combine f g))
  ([f g h] (fn [x] (h (f x) (g x)))))
