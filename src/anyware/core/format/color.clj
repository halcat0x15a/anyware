(ns anyware.core.format.color)

(def default
  (atom {:foreground :black
         :backgraund :white}))

(def color
  (atom {:foreground
         {:special :magenta
          :symbol :blue
          :string :red
          :keyword :cyan}
         :background {}}))

(defn get [part key]
  (if-let [color (get-in [part key] @color)]
    color
    (@default part)))
