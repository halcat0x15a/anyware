(ns felis.buffer
  (:refer-clojure :exclude [read])
  (:require [felis.string :as string]
            [felis.serialization :as serialization]
            [felis.edit :as edit]
            [felis.text :as text]
            [felis.html :as html]
            [felis.syntax :as syntax]))

(defmethod edit/invert :tops [side] :bottoms)

(defmethod edit/invert :bottoms [side] :tops)

(defmethod edit/head :default [buffer field]
  (-> buffer field peek))

(defmethod edit/add :default [{:keys [focus] :as buffer} field focus']
  (-> buffer
      (assoc :focus focus')
      (update-in [field] #(conj % focus))))

(defmethod edit/remove :default [buffer field]
  (if-let [focus' (edit/head buffer field)]
    (-> buffer
        (assoc :focus focus')
        (update-in [field] pop))
    (assoc buffer
      :focus text/default)))

(defn- write [{:keys [tops focus bottoms]}]
  (->> (concat tops (list focus) bottoms)
       (map serialization/write)
       (string/make-string \newline)))

(defn render [syntax {:keys [lefts focus rights] :as buffer}]
  (html/< :pre {:class :buffer}
          (text/tag (->> buffer serialization/write (syntax/highlight syntax)))
          (html/< :span {:class :cursor}
                  (->> (concat lefts (-> focus text/focus list) rights)
                       (map text/cursor)
                       (string/make-string \newline)))))

(defrecord Buffer [focus tops bottoms]
  serialization/Serializable
  (write [buffer] (write buffer)))

(def path [:root :workspace :buffer])

(def default (Buffer. text/default [] '()))

(defn read [string]
  (let [lines (->> string string/split-lines (map text/read))]
    (assoc default
      :focus (first lines)
      :bottoms (->> lines rest (apply list)))))

(defn break [{:keys [focus] :as buffer}]
  (-> buffer
      (update-in [:focus] #(assoc % :rights ""))
      (edit/add :tops (assoc focus :lefts ""))))
