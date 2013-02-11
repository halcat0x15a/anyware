(ns felis.text
  (:refer-clojure :exclude [peek pop conj read])
  (:require [clojure.core :as core]
            [felis.string :as string]
            [felis.edit :as edit]
            [felis.node :as node]
            [felis.serialization :as serialization]
            [felis.default :as default]
            [felis.syntax :as syntax]))

(defmulti peek (fn [feild string] feild))
(defmethod peek :rights [_ string] (first string))
(defmethod peek :lefts [_ string] (last string))

(defmulti pop (fn [feild string] feild))
(defmethod pop :rights [_ string] (string/rest string))
(defmethod pop :lefts [_ string] (string/butlast string))

(defmulti conj (fn [char feild string] feild))
(defmethod conj :rights [char _ string] (str char string))
(defmethod conj :lefts [char _ string] (str string char))

(defn move [text field]
  (if-let [char (->> text field (peek field))]
    (let [field' (edit/opposite field)]
      (-> text
          (update-in [field] (partial pop field))
          (update-in [field'] (partial conj char field'))))
    text))

(defn insert [text field char]
  (update-in text [field] (partial conj char field)))

(defn delete [text field]
  (update-in text [field] (partial pop field)))

(defn write [{:keys [lefts rights]}]
  (str lefts rights))

(defn cursor [{:keys [lefts rights cursor]}]
  (str (node/tag :span {:class :hidden} lefts)
       (node/tag :span {:class cursor} (get rights 0 " "))))

(defn tag [string]
  (node/tag :span {:class :text} string))

(defn render [text]
  (str (-> text write tag)
       (cursor text)))

(defrecord Text [lefts rights cursor]
  edit/Edit
  (move [text field] (move text field))
  (insert [text field char] (insert text field char))
  (delete [text field] (delete text field))
  node/Node
  (render [text] (render text))
  serialization/Serializable
  (write [text] (write text)))

(def path [:root :buffer :focus])

(def lefts (core/conj path :lefts))

(def rights (core/conj path :rights))

(def default (Text. "" "" :pointer))

(defn read [string]
  (assoc default
    :lefts ""
    :rights string))

(defmethod node/path Text [_] path)

(defmethod default/default Text [_] default)

(defmethod serialization/read Text [_ string] (read string))

(defn focus [text]
  (assoc text :cursor :focus))
