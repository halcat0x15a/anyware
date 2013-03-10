(ns anyware.core.lens.protocol
  (:refer-clojure :exclude [get set]))

(defprotocol Lens
  (get [lens obj])
  (set [lens obj value]))
