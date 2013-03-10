(ns anyware.core.lens
  (:refer-clojure :exclude [get set comp name])
  (:require [clojure.zip :as zip]
            [anyware.core.lens.protocol :as protocol]))

(deftype Lens [get set]
  protocol/Lens
  (get [_ obj] (get obj))
  (set [_ value obj] (set obj value)))

(def zip (Lens. zip/node zip/replace))

(defn get [lens obj]
  (if (keyword? lens)
    (lens obj)
    (protocol/get lens obj)))

(defn set [lens value obj]
  (if (keyword? lens)
    (assoc obj lens value)
    (protocol/set lens value obj)))

(defn modify [lens f obj]
  (set lens (f (get lens obj)) obj))

(defn comp [lens' lens]
  (Lens. (fn [obj]
           (->> obj (get lens) (get lens')))
         (fn [obj value]
           (modify lens (partial set lens' value) obj))))
