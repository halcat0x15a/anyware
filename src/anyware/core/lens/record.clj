(ns anyware.core.lens.record
  (:refer-clojure :exclude [comp name])
  (:require [anyware.core.lens :as lens]))

(defn with [lens f]
  (with-meta f {:lens lens}))

(defn safe [f]
  (fn [x] (if-let [y (f x)] y x)))

(defn modify [f editor]
  (if-let [lens (-> f meta :lens)]
    (lens/modify lens f editor)
    (f editor)))

(defn comp [f g]
  (fn [editor]
    (->> editor
         (modify g)
         (modify f))))

(def entry (lens/comp lens/zip :list))

(def name (lens/comp :name entry))

(def history (lens/comp :history entry))

(def change (lens/comp lens/zip history))

(def buffer (lens/comp :buffer change))
