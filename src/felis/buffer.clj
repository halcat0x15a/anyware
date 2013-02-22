(ns felis.buffer
  (:refer-clojure :exclude [read])
  (:require [felis.string :as string]
            [felis.serialization :as serialization]
            [felis.path :as path]
            [felis.edit :as edit]
            [felis.text :as text]))

(defmethod edit/invert :tops [side] :bottoms)

(defmethod edit/invert :bottoms [side] :tops)

(defmethod edit/first :default [field buffer]
  (-> buffer field peek))

(defmethod edit/insert :default [focus' field {:keys [focus] :as buffer}]
  (-> buffer
      (assoc :focus focus')
      (update-in [field] #(conj % focus))))

(defmethod edit/delete :default [field buffer]
  (if-let [focus' (edit/first field buffer)]
    (-> buffer
        (assoc :focus focus')
        (update-in [field] pop))
    (assoc buffer
      :focus text/default)))

(defn texts [{:keys [tops focus bottoms]}]
  (concat tops (list focus) bottoms))

(defn- write [buffer]
  (->> buffer
       texts
       (map serialization/write)
       (string/make-string \newline)))

(defrecord Buffer [focus tops bottoms]
  serialization/Serializable
  (write [buffer] (write buffer)))

(def default (Buffer. text/default [] '()))

(defn read [string]
  (let [lines (->> string string/split-lines (map text/read))]
    (assoc default
      :focus (first lines)
      :bottoms (->> lines rest (apply list)))))

(defn update [f editor]
  (update-in editor path/buffer f))

(defn break [{:keys [focus] :as buffer}]
  (->> (assoc buffer :focus (assoc focus :rights ""))
       (edit/insert (assoc focus :lefts "") :tops)))

(def top (partial update (partial edit/move :tops)))

(def bottom (partial update (partial edit/move :bottoms)))

(def start (partial update (partial edit/end :tops)))

(def end (partial update (partial edit/end :bottoms)))

(def insert-newline
  (partial update (partial edit/insert text/default :bottoms)))

(def append-newline
  (partial update (partial edit/insert text/default :tops)))

(def delete (partial update (partial edit/delete :bottoms)))

(def backspace (partial update (partial edit/delete :tops)))
