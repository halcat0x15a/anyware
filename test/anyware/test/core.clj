(ns anyware.test.core
  (:require [clojure.test :refer (deftest is testing)]
            [anyware.test :as test]
            [anyware.core :as core]
            [anyware.core.lens :as lens]
            [anyware.core.record :as record]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]))

(def anyware
  (reify core/Anyware
    (keycode [this event] event)
    (render [this html] (prn html))))

(defn emulate
  ([editor x]
     (prn editor x)
     (cond (or (keyword? x) (char? x)) (core/run anyware x editor)
           (string? x) (apply emulate editor x)))
  ([editor x & xs]
     (reduce emulate editor (cons x xs))))

(deftest editor
  (testing "type 'hello world'"
    (is (= (->> (emulate editor/default \i "helloworld" :escape \^)
                (lens/get record/buffer))
           (buffer/read "helloworld")))))
