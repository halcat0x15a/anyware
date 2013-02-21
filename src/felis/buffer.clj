(ns felis.buffer
  (:refer-clojure :exclude [read])
  (:require [felis.string :as string]
            [felis.serialization :as serialization]
            [felis.edit :as edit]
            [felis.text :as text]))

(defmethod edit/invert :tops [side] :bottoms)

(defmethod edit/invert :bottoms [side] :tops)

(defmethod edit/head :default [field buffer]
  (-> buffer field peek))

(defmethod edit/insert :default [focus' field {:keys [focus] :as buffer}]
  (-> buffer
      (assoc :focus focus')
      (update-in [field] #(conj % focus))))

(defmethod edit/delete :default [field buffer]
  (if-let [focus' (edit/head field buffer)]
    (-> buffer
        (assoc :focus focus')
        (update-in [field] pop))
    (assoc buffer
      :focus text/default)))

(defn break [{:keys [focus] :as buffer}]
  (->> (update-in buffer [:focus] #(assoc % :rights ""))
       (edit/insert (assoc focus :lefts "") :tops)))

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
