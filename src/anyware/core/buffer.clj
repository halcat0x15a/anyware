(ns anyware.core.buffer
  (:refer-clojure :exclude [char empty read find])
  (:require [clojure.string :as string]))

(defrecord Buffer [left right])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :right))

(defn write [{:keys [left right]}] (str left right))

(def inverse (Buffer. :right :left))

(defmulti append (fn [field _ _] field))
(defmethod append :right [field value buffer]
  (assoc buffer field (str value (field buffer))))
(defmethod append :left [field value buffer]
  (assoc buffer field (str (field buffer) value)))

(defmulti substring (fn [field _ _] field))
(defmethod substring :right [field n buffer]
  (assoc buffer field (subs (field buffer) n)))
(defmethod substring :left [field n buffer]
  (assoc buffer
    field (subs (field buffer) 0 (-> buffer field count (- n)))))

(defn find [regex field buffer]
  (->> buffer field (re-find (field regex))))

(defn split [f regex field buffer]
  (if-let [result (find regex field buffer)]
    (->> buffer (substring field (count result)) (f result))
    buffer))

(defn delete
  ([regex field] (partial delete regex field)) 
  ([regex field buffer]
     (split (fn [_ buffer] buffer) regex field buffer)))

(defn move
  ([regex field] (partial move regex field))
  ([regex field buffer]
     (split (partial append (field inverse)) regex field buffer)))

(def char (Buffer. #"[\s\S]\z" #"\A[\s\S]"))

(def line (Buffer. #"\n??[^\n]*\z" #"\A[^\n]*\n??"))

(def word (Buffer. #"\w+\W*\z" #"\A\W*\w+"))

(def buffer (let [regex #"[\s\S]*"] (Buffer. regex regex)))

(defn times [n]
  (Buffer. (re-pattern (str "[\\s\\S]" \{ n \} "\\z"))
           (re-pattern (str "\\A" "[\\s\\S]" \{ (- n) \}))))

(defn cursor [field buffer]
  (-> buffer field count))

(defn select [buffer]
  (with-meta buffer
    (assoc (meta buffer)
      :mark (cursor :left buffer))))

(defn deselect [buffer]
  (with-meta buffer
    (dissoc (meta buffer) :mark)))

(defn selection [f buffer]
  (if-let [mark (-> buffer meta :mark)]
    (let [n (- (cursor buffer) mark)
          field (cond (pos? n) :left (neg? n) :right)]
      (if field (f (times n) field buffer)))))

(defn command [buffer]
  (-> buffer write (string/split #"\s+")))
