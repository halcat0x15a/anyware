(ns anyware.core.lens
  (:refer-clojure :exclude [get set comp name])
  (:require [clojure.zip :as zip]))

(deftype Lens [get set])

(def zip (Lens. zip/node zip/replace))

(defn get [lens obj]
  (if (keyword? lens)
    (lens obj)
    ((.-get lens) obj)))

(defn set [lens value obj]
  (if (keyword? lens)
    (assoc obj lens value)
    ((.-set lens) obj value)))

(defn modify [lens f obj]
  (set lens (f (get lens obj)) obj))

(defn comp [lens' lens]
  (Lens. (fn [obj] (->> obj (get lens) (get lens')))
         (fn [obj value] (modify lens (partial set lens' value) obj))))

(def entry (comp zip :list))

(def name (comp :name entry))

(def history (comp :history entry))

(def change (comp zip history))

(def buffer (comp :buffer change))
