(ns felis.buffer
  (:refer-clojure :exclude [read peek conj drop pop newline]))

(defrecord Buffer [lefts rights])

(def default (Buffer. "" ""))

(defn write [{:keys [lefts rights]}]
  (str lefts rights))

(defn read [string]
  (Buffer. "" string))

(defmulti invert identity)
(defmethod invert :rights [_] :lefts)
(defmethod invert :lefts [_] :rights)

(defmulti peek (fn [field _] field))
(defmethod peek :rights [field buffer]
  (-> buffer field first))
(defmethod peek :lefts [field buffer]
  (-> buffer field last))

(defmulti conj (fn [field _ _] field))
(defmethod conj :rights [field value buffer]
  (update-in buffer [field] (partial str value)))
(defmethod conj :lefts [field value buffer]
  (update-in buffer [field] #(str % value)))

(defmulti drop (fn [_ field _] field))
(defmethod drop :rights [n field buffer]
  (update-in buffer [field] #(subs % n)))
(defmethod drop :lefts [n field buffer]
  (update-in buffer [field] #(subs % 0 (-> % count (- n)))))

(def pop (partial drop 1))

(defmulti extract (fn [field _] field))
(defmethod extract :rights [_ [_ _ rights]] rights)
(defmethod extract :lefts [_ [_ lefts _]] lefts)

(defn move [field buffer]
  (if-let [char (peek field buffer)]
    (->> buffer
         (conj (invert field) char)
         (pop field))
    buffer))

(def right (partial move :rights))

(def left (partial move :lefts))

(defn skip [regex field buffer]
  (if-let [result (->> buffer field (re-find regex))]
    (let [field' (invert field)]
      (->> (assoc buffer field (extract field result))
           (conj field' (extract field' result))))
    buffer))

(def down (partial skip #"^(.*\n)([\s\S]*)" :rights))

(def up (partial skip #"([\s\S]*)(\n.*)$" :lefts))

(def tail (partial skip #"^(.*)([\s\S]*)" :rights))

(def head (partial skip #"([\s\S]*?)(.*)$" :lefts))

(defn most [field buffer]
  (->> (assoc buffer field "")
       (conj (invert field) (field buffer))))

(def begin (partial most :lefts))

(def end (partial most :rights))

(defn cursor [field buffer]
  (-> buffer field count))

(def append (partial conj :lefts))

(def break (partial conj :lefts \newline))

(def backspace (partial pop :lefts))

(def delete (partial pop :rights))

(def newline (partial conj :lefts \newline))

(def return (partial conj :rights \newline))

(head (tail (read "hell")))
