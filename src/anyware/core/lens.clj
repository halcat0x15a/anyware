(ns anyware.core.lens
  (:refer-clojure :exclude [get set comp name])
  (:require [clojure.zip :as zip]))

(defrecord Lens [get set])

(defmulti get (fn [lens _] (type lens)))
(defmethod get Lens [lens obj] ((:get lens) obj))
(defmethod get :default [lens obj] (lens obj))

(defmulti set (fn [lens _ _] (type lens)))
(defmethod set Lens [lens value obj] ((:set lens) obj value))
(defmethod set :default [lens value obj] (assoc obj lens value))

(defn modify [lens f obj]
  (set lens (f (get lens obj)) obj))

(defn comp [lens' lens]
  (Lens. (fn [obj]
           (->> obj
                (get lens)
                (get lens')))
         (fn [obj value]
           (modify lens (partial set lens' value) obj))))

(def zip (Lens. zip/node zip/replace))

(def name :name)

(def history :history)

(def change (comp zip history))

(def buffer (comp :buffer change))

(def buffers :buffers)

(def minibuffer :minibuffer)

(def mode :mode)
