(ns anyware.web
  (:require [clojure.set :as set]
            [clojure.browser.dom :as dom]
            [anyware.core :as core]
            [anyware.core.html :as html]
            [goog.events :as events]
            [goog.events.KeyCodes :as key]
            [goog.events.KeyHandler.EventType :as type])
  (:import goog.events.KeyHandler))

(def special
  {key/ESC :escape
   key/LEFT :left
   key/RIGHT :right
   key/UP :up
   key/DOWN :down
   key/ENTER \newline
   key/BACKSPACE \backspace})

(deftype Event [event]
  core/Event
  (alt? [this] (.-altKey event))
  (ctrl? [this] (.-ctrlKey event))
  (keycode [this]
    (->> event .-keyCode (.fromCharCode js/String)))
  (keychar [this]
    (get special
         (.-keyCode event)
         (->> event .-charCode (.fromCharCode js/String)))))

(extend-type anyware.core.editor.Editor
  core/Anyware
  (render [editor]
    (->> editor
         html/render
         dom/html->dom
         (dom/replace-node (dom/get-element :editor)))))

(defn main []
  (set! *print-fn* dom/log)
  (doto (KeyHandler. js/window)
    (.addEventListener type/KEY (comp core/run! #(Event. %)))))
