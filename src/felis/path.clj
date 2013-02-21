(ns felis.path
  (:refer-clojure :exclude (name)))

(def root [:root])

(def current (conj root :current))

(def name (conj current :name))

(def buffer (conj current :buffer))

(def focus (conj buffer :focus))

(def history (conj current :history))

(def minibuffer (conj root :minibuffer))

(def environment (conj root :environment))
