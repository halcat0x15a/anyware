(ns anyware.core.parser)

(defprotocol Functor
  (fmap [m f]))

(defrecord Success [value next]
  Functor
  (fmap [success f] (Success. (f value) next)))

(defrecord Failure [next]
  Functor
  (fmap [failure f] failure))

(defn- extract [x]
  (cond (string? x) x
        (vector? x) (first x)))

(defn parse
  ([pattern] (partial parse pattern))
  ([pattern input]
     (if-let [result (->> input (re-find pattern) extract)]
       (Success. result (subs input (count result)))
       (Failure. input))))

(defn parser [pattern]
  (fn [input] (parse pattern input)))

(defn choice [parser & parsers]
  (fn [input]
    (loop [[parser & parsers] (cons parser parsers)]
      (let [{:keys [value next] :as result} (parser input)]
        (if (or value (empty? parsers))
          result
          (recur parsers))))))

(defn chain [f parser & parsers]
  (fn [input]
    (loop [{:keys [value next] :as result} (fmap (parser input) (partial partial f))
           [parser & parsers] parsers]
      (cond (and value (nil? parser)) (fmap result #(%))
            value (recur (fmap (parser next) (partial partial value)) parsers)
            :else (Failure. input)))))

(defn many [parser]
  (fn [input]
    (loop [result [] input input]
      (let [{:keys [value next]} (parser input)]
        (if (= input next)
          (Success. result input)
          (recur (conj result value) next))))))

(defn fail [parser]
  (fn [input]
    (let [{:keys [value] :as result} (parser input)]
      (if value
        (Failure. input)
        (Success. nil input)))))

(defrecord Label [name value])
