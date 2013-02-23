(ns anyware.path
  (:refer-clojure :exclude (name)))

(def current [:current])

(def name (conj current :name))

(def buffer (conj current :buffer))

(def history (conj current :history))

(def minibuffer [:minibuffer])

(def environment [:environment])
