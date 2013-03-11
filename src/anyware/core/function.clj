(ns anyware.core.function)

(defn safe [f]
  (fn [x] (if-let [y (f x)] y x)))
