(ns felis.style)

(def default
  {:body {:margin :0px}
   :.editor {:color :black
             :background-color :white
             :font-size :16px
             :font-family :monospace}
   :.status {:color :black
             :background-color :silver
             :margin :0px}
   :.buffer {:position :relative
             :margin :0px}
   :.text {:position :absolute}
   :.cursor {:position :absolute
             :top :0px
             :left :0px}
   :.hidden {:visibility :hidden}
   :.pointer {:color :white
              :background-color :gray}
   :.focus {:color :white
            :background-color :black}
   :.minibuffer {:position :fixed
                 :bottom :0px
                 :margin :0px}
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
