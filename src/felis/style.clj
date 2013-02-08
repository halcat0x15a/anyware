(ns felis.style)

(def default
  {:body {:margin :0px}
   :.editor {:color :black
             :background-color :white
             :font-size :16px
             :font-family :monospace}
   :.cursor {:position :absolute
             :left :0px}
   :.hidden {:visibility :hidden}
   :.pointer {:color :white
              :background-color :gray}
   :.focus {:color :white
            :background-color :black}
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
