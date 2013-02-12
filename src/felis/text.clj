(ns felis.text
  (:refer-clojure :exclude [peek pop read])
  (:require [clojure.core :as core]
            [felis.string :as string]
            [felis.html :as html]
            [felis.edit :as edit]
            [felis.syntax :as syntax]))

(defmethod edit/invert :lefts [side] :rights)

(defmethod edit/invert :rights [side] :lefts)

(defmethod edit/head :rights [text field]
  (-> text field first))

(defmethod edit/head :lefts [text field]
  (-> text field last))

(defmethod edit/add :rights [text field char]
  (update-in text [field] (partial str char)))

(defmethod edit/add :lefts [text field char]
  (update-in text [field] #(str % char)))

(defmethod edit/remove :rights [text field]
  (let [string (field text)]
    (if (empty? string)
      text
      (assoc text
        field (subs string 1)))))

(defmethod edit/remove :lefts [text field]
  (let [string (field text)]
    (if (empty? string)
      text
      (assoc text
        field (subs string 0 (-> string count dec))))))

(defn write [{:keys [lefts rights]}]
  (str lefts rights))

(defn cursor [{:keys [lefts rights cursor]}]
  [(html/->Node :span {:class :hidden} lefts)
   (html/->Node :span {:class cursor} (get rights 0 " "))])

(defn tag [string]
  (html/->Node :span {:class :text} string))

(defn render [text]
  (str (-> text write tag)
       (cursor text)))

(defrecord Text [lefts rights cursor])

(def path [:root :workspace :buffer :focus])

(def lefts (conj path :lefts))

(def rights (conj path :rights))

(def minibuffer [:root :minibuffer])

(def default (Text. "" "" :pointer))

(defn read [string]
  (assoc default
    :lefts ""
    :rights string))

(defn focus [text]
  (assoc text :cursor :focus))
