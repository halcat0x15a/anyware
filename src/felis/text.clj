(ns felis.text
  (:refer-clojure :exclude [peek pop conj update-in])
  (:require [clojure.core :as core]
            [felis.string :as string]
            [felis.collection :as collection]
            [felis.edit :as edit]
            [felis.node :as node]
            [felis.serialization :as serialization]
            [felis.default :as default]
            [felis.syntax :as syntax]))

(def tag #"^<.+?>")

(defmulti peek (fn [feild string] feild))
(defmethod peek :rights [_ string] (first string))
(defmethod peek :lefts [_ string] (last string))

(defmulti pop (fn [feild string] feild))
(defmethod pop :rights [_ string] (string/rest string))
(defmethod pop :lefts [_ string] (string/butlast string))

(defmulti conj (fn [char feild string] feild))
(defmethod conj :rights [char _ string] (str char string))
(defmethod conj :lefts [char _ string] (str string char))

(defn update-in [text field f]
  (core/update-in text [field] (partial f field)))

(defn move [text field]
  (if-let [char (->> text field (peek field))]
    (-> text
        (update-in field pop)
        (update-in (edit/opposite field) (partial conj char)))
    text))

(defn insert [text field char]
  (update-in text field (partial conj char)))

(defn delete [text field]
  (update-in text field pop))

(defn serialize [{:keys [lefts rights]}]
  (str lefts rights))

(defn cursor [{:keys [lefts rights cursor]}]
  (str (node/tag :span {:class :hidden} lefts)
       (node/tag :span {:class cursor} (get rights 0 " "))))

(defn tag [string]
  (node/tag :span {:class :text} string))

(defn render [text]
  (str (-> text serialize tag)
       (cursor text)))

(defrecord Text [lefts rights cursor]
  edit/Edit
  (move [text field] (move text field))
  (insert [text field char] (insert text field char))
  (delete [text field] (delete text field))
  node/Node
  (render [text] (render text))
  serialization/Serializable
  (serialize [text] (serialize text)))

(def path [:root :buffer :focus])

(defn update [update editor]
  (core/update-in editor path update))

(def lefts (core/conj path edit/lefts))

(def rights (core/conj path edit/rights))

(def default (Text. "" "" :pointer))

(defn deserialize [string]
  (assoc default
    :lefts ""
    :rights string))

(defmethod node/path Text [_] path)

(defmethod default/default Text [_] default)

(defmethod serialization/deserialize Text [_ string]
  (deserialize string))

(defn focus [text]
  (assoc text :cursor :focus))
