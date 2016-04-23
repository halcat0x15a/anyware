(ns anyware.buffer)

(defrecord Buffer [left right])

(def default (Buffer. "" ""))

(defn insert [value field buffer]
  (let [text (get buffer field)
        text (case field
                :left (str text value)
                :right (str value text))]
    (assoc buffer field text)))

(defn delete [n field buffer]
  (let [text (get buffer field)
        text (case field
                :left (subs text 0 (- (count text) n))
                :right (subs text n))]
    (assoc buffer field text)))

(defn move [n field buffer]
  (let [text (get buffer field)
        text (case field
               :left (subs text n)
               :right (subs text 0 n))
        field' (case field
                 :left :right
                 :right :left)]
    (->> buffer (delete n field) (insert text field'))))

(defn text [buffer]
  (str (:left buffer) (:right buffer)))
