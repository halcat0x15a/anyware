(ns felis.test.core
  (:require [clojure.test :refer (deftest is testing)]
            [felis.test :as test]
            [felis.path :as path]
            [felis.core :as core]
            [felis.editor :as editor]
            [felis.buffer :as buffer]))

(def keycode
  (reify core/KeyCode
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
    (core/run editor keycode this))
  clojure.lang.Keyword
  (input [this editor]
    (core/run editor keycode this)))

(defn emulate [editor x & xs]
  (reduce (fn [editor x] (input x editor)) editor (cons x xs)))

(deftest editor
  (testing "type 'hello world'"
    (is (= (-> editor/default
               (emulate \i "helloworld" :escape \0)
               (get-in path/buffer))
           (buffer/read "helloworld")))))
