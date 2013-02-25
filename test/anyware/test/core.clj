(ns anyware.test.core
  (:require [clojure.test :refer (deftest is testing)]
            [anyware.test :as test]
            [anyware.lens :as lens]
            [anyware.core :as core]
            [anyware.editor :as editor]
            [anyware.buffer :as buffer]))

(def anyware
  (reify core/Anyware
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
    (core/run anyware this editor))
  clojure.lang.Keyword
  (input [this editor]
    (core/run anyware this editor)))

(defn emulate [editor x & xs]
  (reduce (fn [editor x] (input x editor)) editor (cons x xs)))

(deftest editor
  (testing "type 'hello world'"
    (is (= (->> (emulate editor/default \i "helloworld" :escape \0)
                (lens/get lens/buffer))
           (buffer/read "helloworld")))))
