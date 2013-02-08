(ns felis.style)

(def default
  {:.editor {:color :black
             :background-color :white
             :font-size :16px
             :font-family :monospace}
   :.focus {:text-decoration :blink
            :color :white
            :background-color :black}
   :.pointer {:color :white
              :background-color :gray}
   :.minibuffer {:position :absolute
                 :bottom :0px}
   :.name {:color :blue}
   :.special {:color :fuchsia}
   :.string {:color :red}
   :.keyword {:color :aqua}
   :.comment {:color :maroon}})

(defn css [style]
  (reduce-kv (fn [string selector block]
               (let [selector (name selector)
                     block (reduce-kv (fn [block property value]
                                        (str block \space (name property) \: \space (name value) \;))
                                      ""
                                      block)]
                 (str string \space selector \space \{ block \space \})))
             ""
             style))
