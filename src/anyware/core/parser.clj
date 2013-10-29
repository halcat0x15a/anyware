(ns anyware.core.parser)

(defprotocol Functor
  (fmap [m f]))

(defrecord Success [value next]
  Functor
  (fmap [success f] (Success. (f value) next)))

(defrecord Failure [next]
  Functor
  (fmap [failure f] failure))

(defprotocol Parser
  (parse [parser input]))

(extend-protocol Functor
  ;*CLJSBUILD-REMOVE*;js/Function #_
  clojure.lang.Fn
  (fmap [parser f] (fn [input] (fmap (parse parser input) f)))
  ;*CLJSBUILD-REMOVE*;object #_
  java.lang.Object
  (fmap [parser f] (fmap (partial parse parser) f)))

(defn- extract [x]
  (cond (string? x) x
        (vector? x) (first x)))

(extend-protocol Parser
  ;*CLJSBUILD-REMOVE*;string #_
  java.lang.String
  (parse [string input]
    (let [length (count string)]
      (cond (< (count input) length) (Failure. input)
            (= (subs input 0 length) string)
            (Success. string (subs input length))
            :else (Failure. input))))
  ;*CLJSBUILD-REMOVE*;js/RegExp #_
  java.util.regex.Pattern
  (parse [pattern input]
    (if-let [result (->> input (re-find pattern) extract)]
      (Success. result (subs input (count result)))
      (Failure. input)))
  ;*CLJSBUILD-REMOVE*;js/Function #_
  clojure.lang.Fn
  (parse [parser input] (parser input)))

(defn <|> [parser & parsers]
  (fn [input]
    (loop [[parser & parsers] (cons parser parsers)]
      (let [{:keys [value next] :as result} (parse parser input)]
        (if (or value (empty? parsers))
          result
          (recur parsers))))))

(defn <*> [f parser & parsers]
  (fn [input]
    (loop [{:keys [value next] :as result}
           (fmap (parse parser input) (partial partial f))
           [parser & parsers] parsers]
      (cond (and value (nil? parser)) (fmap result #(%))
            value (recur (fmap (parse parser next) (partial partial value)) parsers)
            :else (Failure. input)))))

(defn many [parser]
  (fn [input]
    (loop [result [] input input]
      (let [{:keys [value next]} (parse parser input)]
        (if (= input next)
          (Success. result input)
          (recur (conj result value) next))))))
