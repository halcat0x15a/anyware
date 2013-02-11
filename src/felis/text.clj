(ns felis.text
  (:refer-clojure :exclude [peek pop read])
  (:require [clojure.core :as core]
            [felis.string :as string]
            [felis.html :as html]
            [felis.edit :as edit]
            [felis.syntax :as syntax]))

(defmulti peek (fn [_ feild] feild))

(defmethod peek :rights [text field]
  (-> text field first))

(defmethod peek :lefts [text field]
  (-> text field last))

(defmulti pop (fn [_ feild] feild))

(defmethod pop :rights [text field]
  (update-in text [field] #(subs % 1)))

(defmethod pop :lefts [text field]
  (update-in text [field]
             (fn [string]
               (subs string 0 (-> string count dec)))))

(defmulti insert (fn [_ feild _] feild))

(defmethod insert :rights [text field char]
  (update-in text [field] (partial str char)))

(defmethod insert :lefts [text field char]
  (update-in text [field] #(str % char)))

(defn move [text field]
  (if-let [char (peek text field)]
    (-> text
        (pop field)
        (insert (edit/opposite field) char))
    text))

(defn delete [text field]
  (if (empty? (field text))
    text
    (pop text field)))

(defn write [{:keys [lefts rights]}]
  (str lefts rights))

(defn cursor [{:keys [lefts rights cursor]}]
  (str (html/tag :span {:class :hidden} lefts)
       (html/tag :span {:class cursor} (get rights 0 " "))))

(defn tag [string]
  (html/tag :span {:class :text} string))

(defn render [text]
  (str (-> text write tag)
       (cursor text)))

(defrecord Text [lefts rights cursor]
  edit/Edit
  (move [text field] (move text field))
  (insert [text field char] (insert text field char))
  (delete [text field] (delete text field)))

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
