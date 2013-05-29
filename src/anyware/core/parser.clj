(ns anyware.core.parser)

(defprotocol Parser
  (parse [parser input]))

(defprotocol Functor
  (fmap [x f]))

(defprotocol Monad
  (bind [x f]))

(defprotocol Plus
  (plus [x y]))

(defprotocol Result
  (extract [x]))

(defrecord Success [result next]
  Functor
  (fmap [success f] (Success. (f result) next))
  Monad
  (bind [success f] (f result next))
  Plus
  (plus [success result] success))

(defrecord Failure [next]
  Functor
  (fmap [failure f] failure)
  Monad
  (bind [failure f] failure)
  Plus
  (plus [failure result] result))

(extend-protocol Result
  java.lang.String
  (extract [string] string)
  clojure.lang.IPersistentVector
  (extract [vector] (first vector))
  nil
  (extract [_] nil))

(extend-protocol Functor
  clojure.lang.IFn
  (fmap [parser f] (fn [input] (fmap (parse parser input) f))))

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
    (reduce (fn [result parser]
              (plus result (parse parser input)))
            (parse parser input)
            parsers)))

(defn product [parser & parsers]
  (fn [input]
    (reduce (fn [result parser]
              (bind result
                    (fn [value input]
                      (fmap (parse parser input)
                            (partial conj value)))))
            (fmap (parse parser input) vector)
            parsers)))

(defn many [parser]
  (fn [input]
    (loop [result' [] input input]
      (let [{:keys [result next]} (parse parser input)]
        (if result
          (recur (conj result' result) next)
          (Success. result' input))))))

(defn id [x] (Success. x ""))
