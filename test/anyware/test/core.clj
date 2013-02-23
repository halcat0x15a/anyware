(ns anyware.test.core
  (:require [clojure.test :refer (deftest is testing)]
            [anyware.test :as test]
            [anyware.path :as path]
            [anyware.core :as core]
            [anyware.editor :as editor]
            [anyware.buffer :as buffer]))

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
