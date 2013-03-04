(ns anyware.web
  (:require [anyware.core :as core]
            [goog.dom :as dom]
            [goog.events.KeyCodes :as key]))

(def special
  {key/ESC :escape
   key/LEFT :left
   key/RIGHT :right
   key/UP :up
   key/DOWN :down
   key/BACKSPACE :backspace
   key/ENTER :enter})

(def anyware
  (reify core/Anyware
    (keycode [this event]
      (if-let [key (-> special .-keyCode event)]
        key
        (.-charCode event)))
    (render [this html]
      (->> "html"
           dom/getElementsByTagNameAndClass
           first
           (html dom/htmlToDocumentFragment dom/replaceNode)))))
