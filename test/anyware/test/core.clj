(ns anyware.test.core
  (:refer-clojure :exclude [type])
  (:require [clojure.test :refer (deftest is testing)]
            [anyware.test :as test]
            [anyware.core :as core]
            [anyware.core.record :as record]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]))

(def anyware
  (reify core/Anyware
    (keycode [this event] event)
    (render [this html] (prn html))))

(defn emulate
  ([editor x]
     (cond (or (keyword? x) (char? x)) (core/run editor anyware x)
           (string? x) (apply emulate editor x)))
  ([editor x & xs]
     (reduce emulate editor (cons x xs))))

(def type
  (comp buffer/write
        (record/get record/buffer)
        (partial emulate editor/default)))

(deftest editor
  (testing "type 'hello world'"
    (is (= (type \i "hello world" :escape \^)
           "hello world")))
  (testing "hello world in clojure"
    (is (= (type \i
                 "(defn helloworld []" :enter
                 "  (prn \"hello world\"))":escape \^)
           "(defn helloworld []
  (prn \"hello world\"))"))))
