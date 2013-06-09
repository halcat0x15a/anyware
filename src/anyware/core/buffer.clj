(ns anyware.core.buffer
  (:refer-clojure :exclude [char empty read find take])
  (:require [clojure.string :as string]))

(defrecord Buffer [left right])

(def empty (Buffer. "" ""))

(def read (partial assoc empty :right))

(defn write [{:keys [left right]}] (str (-> left reverse string/join) right))

(def inverse
  {:right :left
   :left :right})

(defprotocol Text
  (insert [text field buffer]))

(extend-protocol Text
  java.lang.Character
  (insert [char field buffer]
    (assoc buffer
      field (str char (field buffer))))
  java.lang.String
  (insert [string field buffer]
    (assoc buffer
      field (str (string/reverse string) (field buffer)))))

(defn field [n]
  (cond (pos? n) :left
        (neg? n) :right))

(defn substring
  ([n buffer]
     (if-let [f (field n)]
       (substring n f buffer)
       buffer))
  ([n field buffer]
     (update-in buffer [field] #(subs % n))))

(defn takes
  ([n buffer]
     (if-let [f (field n)] (takes n f buffer) ""))
  ([n field buffer]
     (subs (field buffer) 0 n)))

(defn delete
  ([f field] (partial delete f field)) 
  ([f field buffer]
     (if-let [result (->> buffer field f)]
       (substring (count result) field buffer)
       buffer)))

(defn move
  ([f field] (partial move f field))
  ([f field buffer]
     (if-let [result (->> buffer field f)]
       (->> buffer
            (substring (count result) field)
            (insert result (inverse field)))
       buffer)))

(def char (comp str first))

(def line (partial re-find #"^[^\n]*\n??"))

(def word (partial re-find #"^\W*\w+"))

(defn cursor
  ([buffer] (cursor :left buffer))
  ([field buffer] (-> buffer field count)))

(def select #(vary-meta % assoc :mark (cursor :left %)))

(def deselect #(vary-meta % dissoc :mark))

(defn selection [f buffer]
  (if-let [mark (-> buffer meta :mark)]
    (f (- (cursor buffer) mark) buffer)))

(def copy (partial selection takes))

(def cut (partial selection drop))

(defn command [buffer]
  (-> buffer write (string/split #"\s+")))
