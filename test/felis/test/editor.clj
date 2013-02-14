(ns felis.test.editor
  (:require [clojure.test :refer :all]
            [felis.key :as key]
            [felis.main :as main]
            [felis.editor :as editor]
            [felis.editor.normal :as normal]
            [felis.buffer :as buffer]
            [felis.text :as text]))

(def keycode
  (reify editor/KeyCode
    (code [this event] event)))

(defprotocol Input
  (input [this editor]))

(extend-protocol Input
  java.lang.String
  (input [this editor]
    (if-let [char (first this)]
      (->> editor
           (input char)
           (input (subs this 1)))
      editor))
  java.lang.Character
  (input [this editor]
    (main/run editor keycode this))
  clojure.lang.Keyword
  (input [this editor]
    (main/run editor keycode this)))

(defn emulate [editor x & xs]
  (reduce (fn [editor x] (input x editor)) editor (cons x xs)))

(deftest editor
  (testing "type 'hello world'"
    (is (= (emulate normal/default \i "helloworld" key/escape)
           (assoc-in normal/default text/lefts "helloworld"))))
  (testing "move on all sides"
    (is (= (-> normal/default
               (assoc-in buffer/path (buffer/read "hello\nworld"))
               (emulate \l \j \k \h))
           (assoc-in normal/default buffer/path (buffer/read "hello\nworld"))))))
