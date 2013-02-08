(ns felis.buffer
  (:require [felis.string :as string]
            [felis.collection :as collection]
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
        (update-in [field] collection/pop)
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
        (update-in [field] collection/pop))
    buffer))

(defn render [{:keys [path lefts focus rights syntax]}]
  (let [render (partial text/render syntax)]
    (str (node/tag :p {:class :status} (name path))
         (node/tag :pre {:class :buffer}
                   (->> (concat (map render lefts)
                                (-> focus (assoc :cursor :focus) render list)
                                (map render rights))
                        (string/make-string "<br>"))))))

(defn serialize [{:keys [lefts focus rights]}]
  (->> (concat lefts (list focus) rights)
       (map text/serialize)
       (string/make-string \newline)))

(defrecord Buffer [path focus lefts rights syntax]
  edit/Edit
  (move [buffer field] (move buffer field))
  (insert [buffer field focus] (insert buffer field focus))
  (delete [buffer field] (delete buffer field))
  node/Node
  (render [buffer] (render buffer))
  serialization/Serializable
  (serialize [buffer] (serialize buffer)))

(def path [:root :buffer])

(defn update [update editor]
  (update-in editor path update))

(def default (Buffer. :*scratch* text/default [] '() syntax/default))

(defn deserialize [string]
  (let [lines (->> string string/split-lines (map text/deserialize))]
    (assoc default
      :focus (first lines)
      :rights (->> lines rest (apply list)))))

(defmethod node/path Buffer [_] path)

(defmethod default/default Buffer [_] default)

(defmethod serialization/deserialize Buffer [_ string] (deserialize string))
