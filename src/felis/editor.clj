(ns felis.editor
  (:require [felis.key :as key]
            [felis.html :as html]
            [felis.root :as root]))

(defprotocol Editor
  (keymap [editor])
  (input [editor char]))

(defprotocol KeyCode
  (code [this event]))

(defn render [{:keys [root]}]
  (html/write
   (html/< :html {} (root/render root))))
