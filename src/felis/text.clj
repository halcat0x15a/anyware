(ns felis.text
  (:refer-clojure :exclude [read])
  (:require [felis.string :as string]
            [felis.serialization :as serialization]
            [felis.edit :as edit]
            [felis.syntax :as syntax]))

(defmethod edit/invert :lefts [side] :rights)

(defmethod edit/invert :rights [side] :lefts)

(defmethod edit/head :rights [field text]
  (-> text field first))

(defmethod edit/head :lefts [field text]
  (-> text field last))

(defmethod edit/insert :rights [char field text]
  (update-in text [field] (partial str char)))

(defmethod edit/insert :lefts [char field text]
  (update-in text [field] #(str % char)))

(defmethod edit/delete :rights [field text]
  (let [string (field text)]
    (if (empty? string)
      text
      (assoc text
        field (subs string 1)))))

(defmethod edit/delete :lefts [field text]
  (let [string (field text)]
    (if (empty? string)
      text
      (assoc text
        field (subs string 0 (-> string count dec))))))

(defn- write [{:keys [lefts rights]}]
  (str lefts rights))

(defrecord Text [lefts rights]
  serialization/Serializable
  (write [text] (write text)))

(def default (Text. "" ""))

(defn read [string]
  (assoc default
    :lefts ""
    :rights string))
