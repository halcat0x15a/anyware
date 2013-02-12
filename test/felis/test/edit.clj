(ns felis.test.edit
  (:require [clojure.test.generative :refer :all]
            [clojure.data.generators :as gen]
            [felis.test :as test]
            [felis.edit :as edit]))

(defprotocol Element
  (element [edit]))

(extend-protocol Element
  felis.buffer.Buffer
  (element [edit] (test/text))
  felis.text.Text
  (element [edit] (gen/char)))

(defprotocol Field
  (field [edit]))

(extend-protocol Field
  felis.buffer.Buffer
  (field [edit]
    (gen/rand-nth [:tops :bottoms]))
  felis.text.Text
  (field [edit]
    (gen/rand-nth [:lefts :rights])))

(defn edit []
  ((gen/rand-nth [test/text test/buffer])))

(defn edit*field []
  (let [edit (edit)]
    [edit (field edit)]))

(defn edit*field*element []
  (let [edit (edit)]
    [edit (field edit) (element edit)]))

(defspec get-field
  field
  [^{:tag `edit} edit]
  (is (not (nil? %))))

(defspec invert-invert
  (fn [[_ field]]
    (-> field edit/invert edit/invert))
  [^{:tag `edit*field} [edit field]]
  (is (= % field)))

(defspec add-remove
  (fn [[edit field]]
    (-> edit
        (edit/add field (element edit))
        (edit/remove field)))
  [^{:tag `edit*field} [edit field]]
  (is (= % edit)))

(defspec move-move
  (fn [[edit field]]
    (-> edit
        (edit/add field (element edit))
        (edit/move field)
        (edit/move (edit/invert field))
        (edit/remove field)))
  [^{:tag `edit*field} [edit field]]
  (is (= % edit)))

(defspec end-head
  (fn [[edit field]]
    (-> edit
        (edit/end field)
        (edit/head field)))
  [^{:tag `edit*field} [edit field]]
  (is (nil? %)))
