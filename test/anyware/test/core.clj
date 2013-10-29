(ns anyware.test.core
  (:refer-clojure :exclude [type])
  (:require [clojure.test :refer (deftest is testing)]
            [anyware.test :as test]
            [anyware.core :as core]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]
            [anyware.core.keys :as keys]
            [anyware.core.api :as api]))

(deftype Event [key]
  core/Event
  (alt? [event] false)
  (ctrl? [event] false)
  (keycode [event] key)
  (keychar [event] key))

(extend-type anyware.core.editor.Editor
  core/Anyware
  (render [this] (prn this))
  (quit [this]))

(defn emulate
  ([editor x]
     (cond (or (keyword? x) (char? x) (set? x)) (core/run editor (Event. x))
           (string? x) (apply emulate editor x)))
  ([editor x & xs]
     (reduce emulate editor (cons x xs))))

(def type
  (comp buffer/show
        #(get-in % keys/buffer)
        (partial emulate editor/default)))

(deftest editor
  (testing "type 'hello world'"
    (is (= (type "hello world") "hello world")))
  (testing "move cursor"
    (is (= (type "fizz" :left :left :left :left
                 "1 2 " :right :right :right :right
                 " 4 buzz")
           "1 2 fizz 4 buzz")))
  (testing "copy and paste"
    (is (= (type "hoge"
                 #{:shift :left} #{:shift :left} #{:shift :left} #{:shift :left}
                 #{:ctrl \C} #{:ctrl \V})
           "hogehoge")))
  (testing "cut and paste"
    (is (= (type "fugafuga"
                 #{:shift :left} #{:shift :left} #{:shift :left} #{:shift :left}
                 #{:ctrl \X} :left :left #{:ctrl \V})
           "fufugaga")))
  (testing "undo and redo"
    (is (= (type "hello" #{:ctrl \Z} #{:ctrl :shift \Z}) "hello")))
  (testing "create new buffer"
    (is (= (type "foo" #{:alt \M} "new foo" \newline) ""))))
