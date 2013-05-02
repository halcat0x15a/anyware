(ns anyware.core.path)

(def frame [:frame])

(def mode [:mode])

(def clipboard [:clipboard])

(def history (conj frame 0))

(def change (conj history 0))

(def buffer (conj change :current))

(def command [:command])

(def minibuffer (-> command (conj 0) (conj :current)))
