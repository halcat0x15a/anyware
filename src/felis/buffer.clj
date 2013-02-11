(ns felis.buffer
  (:refer-clojure :exclude [read])
  (:require [felis.string :as string]
            [felis.edit :as edit]
            [felis.text :as text]
            [felis.serialization :as serialization]
            [felis.node :as node]
            [felis.default :as default]
            [felis.syntax :as syntax]))

(defn move [{:keys [focus] :as buffer} field]
  (if-let [focus' (-> buffer field peek)]
    (-> buffer
        (assoc :focus focus')
        (update-in [field] pop)
        (update-in [(edit/opposite field)] #(conj % focus)))
    buffer))

(defn insert [{:keys [focus] :as buffer} field focus']
  (-> buffer
      (assoc :focus focus')
      (update-in [field] #(conj % focus))))

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

(defn render [{:keys [path lefts focus rights syntax] :as buffer}]
  (str (node/tag :p {:class :status} (name path))
       (node/tag :pre {:class :buffer}
                 (text/tag (->> buffer write (syntax/highlight syntax)))
                 (node/tag :span {:class :cursor}
                           (->> (concat lefts (-> focus text/focus list) rights)
                                (map text/cursor)
                                (string/make-string \newline))))))

(defrecord Buffer [path focus lefts rights syntax]
  edit/Edit
  (move [buffer field] (move buffer field))
  (insert [buffer field focus] (insert buffer field focus))
  (delete [buffer field] (delete buffer field))
  node/Node
  (render [buffer] (render buffer))
  serialization/Serializable
  (write [buffer] (write buffer)))

(def path [:root :buffer])

(def default (Buffer. :*scratch* text/default [] '() syntax/default))

(defn read [string]
  (let [lines (->> string string/split-lines (map text/read))]
    (assoc default
      :focus (first lines)
      :rights (->> lines rest (apply list)))))

(defmethod node/path Buffer [_] path)

(defmethod default/default Buffer [_] default)

(defmethod serialization/read Buffer [_ string] (read string))

(defn break [field {:keys [focus] :as buffer}]
  (-> buffer
      (assoc :focus (assoc focus field ""))
      (insert (edit/opposite field)
              (assoc focus (edit/opposite field) ""))))
