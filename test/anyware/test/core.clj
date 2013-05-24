(ns anyware.test.core
  (:refer-clojure :exclude [type])
  (:require [clojure.test :refer (deftest is testing)]
            [anyware.test :as test]
            [anyware.core :as core]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer]
            [anyware.core.keys :as keys]
            [anyware.core.api :as api]))

(extend-type anyware.core.editor.Editor
  core/Anyware
  (keycode [this event] event)
  (render [this] (prn this))
  (quit [this]))

(defn emulate
  ([editor x]
     (cond (or (keyword? x) (char? x)) (core/run editor x)
           (string? x) (apply emulate editor x)))
  ([editor x & xs]
     (reduce emulate editor (cons x xs))))

(def type
  (comp buffer/write
        #(get-in % keys/buffer)
        (partial emulate editor/default)))

(deftest editor
  (testing "type 'hello world'"
    (is (= (type "hello world") "hello world"))))
