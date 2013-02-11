(ns felis.lisp.environment)

(def path [:root :environment])

(defn add [editor key function]
  (update-in editor path #(assoc % key function)))

(def global
  {'+ +
   '- -
   '* *
   '= =
   'dec dec
   'zero? zero?
   'comp comp
   'map map
   'dorun dorun})
