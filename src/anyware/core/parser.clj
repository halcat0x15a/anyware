(ns anyware.core.parser)

(defprotocol Functor
  (fmap [x f]))

(defprotocol Parser
  (parse [parser input]))

(defprotocol Result
  (extract [x]))

(defrecord Success [value next]
  Functor
  (fmap [success f] (Success. (f value) next)))

(defrecord Failure [next]
  Functor
  (fmap [failure f] failure))

(extend-protocol Functor
  clojure.lang.IFn
  (fmap [parser f] (fn [input] (fmap (parse parser input) f))))

(extend-protocol Result
  java.lang.String
  (extract [string] string)
  clojure.lang.IPersistentVector
  (extract [vector] (first vector))
  nil
  (extract [_]))

(extend-protocol Parser
  java.lang.Character
  (parse [char input]
    (if (identical? (first input) char)
      (Success. char (subs input 1))
      (Failure. input)))
  java.lang.String
  (parse [string input]
    (let [length (count string)]
      (cond (< (count input) length) (Failure. input)
            (= (subs input 0 length) string)
            (Success. string (subs input length))
            :else (Failure. input))))
  java.util.regex.Pattern
  (parse [pattern input]
    (if-let [result (->> input (re-find pattern) extract)]
      (Success. result (subs input (count result)))
      (Failure. input)))
  clojure.lang.Fn
  (parse [parser input] (parser input)))

(defn sum [parser & parsers]
  (fn [input]
    (loop [[parser & parsers] (cons parser parsers)]
      (let [{:keys [value next] :as result} (parse parser input)]
        (if (or value (nil? parser))
          result
          (recur parsers))))))

(defn product [parser & parsers]
  (fn [input]
    (loop [{:keys [value next] :as result}
           (fmap (parse parser input) vector)
           [parser & parsers] parsers]
      (cond (nil? parser) result
            value (recur (fmap (parse parser next)
                               (partial conj value))
                         parsers)
            :else (Failure. input)))))

(defn many [parser]
  (fn [input]
    (loop [result [] input input]
      (let [{:keys [value next]} (parse parser input)]
        (if value
          (recur (conj result value) next)
          (Success. result input))))))

(defn id [x] (Success. x ""))
