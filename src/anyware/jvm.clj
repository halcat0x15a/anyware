(ns anyware.jvm
  (:require [anyware.core :as core]
            [anyware.core.command :as command]
            [anyware.jvm.file :as file])
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application]
           [javafx.event EventHandler]
           [javafx.scene Scene]
           [javafx.scene.web WebView]
           [javafx.scene.input KeyCode]))

(def special
  {KeyCode/ESCAPE :escape
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/BACK_SPACE :backspace
   KeyCode/ENTER :enter})

(defrecord Anyware [view]
  core/Anyware
  (keycode [this event]
    (if-let [key (special (.getCode event))]
      key
      (-> event .getText first)))
  (render [this html]
    (.. view getEngine (loadContent html))))

(defmethod command/exec "open" file/open)
(defmethod command/exec "save" file/save)

(defn -start [this stage]
  (let [view (WebView.)
        anyware (Anyware. view)]
    (doto stage
      (.setTitle "Anyware")
      (.setScene (doto (Scene. view)
                   (.setOnKeyPressed (reify EventHandler
                                       (handle [this event]
                                         (core/run anyware event))))))
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
