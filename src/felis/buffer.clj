(ns felis.buffer
  (:refer-clojure :exclude [read])
  (:require [felis.string :as string]
            [felis.edit :as edit]
            [felis.text :as text]
            [felis.html :as html]
            [felis.syntax :as syntax]))

(defn insert [{:keys [focus] :as buffer} field focus']
  (-> buffer
      (assoc :focus focus')
      (update-in [field] #(conj % focus))))

(defn move [{:keys [focus] :as buffer} field]
  (if-let [focus' (-> buffer field peek)]
    (-> buffer
        (update-in [field] pop)
        (insert (edit/opposite field) focus'))
    buffer))

(defn delete [buffer field]
  (if-let [focus' (-> buffer field peek)]
    (-> buffer
        (assoc :focus focus')
        (update-in [field] pop))
    (assoc buffer
      :focus text/default)))

(defn write [{:keys [lefts focus rights]}]
  (->> (concat lefts (list focus) rights)
       (map text/write)
       (string/make-string \newline)))

(defn render [syntax {:keys [lefts focus rights] :as buffer}]
  (html/tag :pre {:class :buffer}
            (text/tag (->> buffer write (syntax/highlight syntax)))
            (html/tag :span {:class :cursor}
                      (->> (concat lefts (-> focus text/focus list) rights)
                           (map text/cursor)
                           (string/make-string \newline)))))

(defrecord Buffer [focus lefts rights]
  edit/Edit
  (move [buffer field] (move buffer field))
  (insert [buffer field focus] (insert buffer field focus))
  (delete [buffer field] (delete buffer field)))

(def path [:root :workspace :buffer])

(def default (Buffer. text/default [] '()))

(defn read [string]
  (let [lines (->> string string/split-lines (map text/read))]
    (assoc default
      :focus (first lines)
      :rights (->> lines rest (apply list)))))

(defn break [field {:keys [focus] :as buffer}]
  (-> buffer
      (assoc :focus (assoc focus field ""))
      (insert (edit/opposite field)
              (assoc focus (edit/opposite field) ""))))
