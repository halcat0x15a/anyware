(ns anyware.jvm
  (:require [anyware.core :as core]
            [anyware.core.command :as command]
            [anyware.jvm.file :as file])
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application Platform]
           [javafx.event EventHandler]
           [javafx.stage Stage]
           [javafx.scene Scene]
           [javafx.scene.web WebView]
           [javafx.scene.input KeyCode KeyEvent]))

(def special
  {KeyCode/ESCAPE :escape
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/BACK_SPACE :backspace
   KeyCode/ENTER :enter})

(defrecord Anyware [^WebView view]
  core/Anyware
  (keycode [this event]
    (let [event ^KeyEvent event]
      (if-let [key (-> event .getCode special)]
        key
        (-> event .getText first))))
  (render [this html]
    (.. view getEngine (loadContent html))))

(defmethod command/exec "quit" [_ _] (Platform/exit))

(defn -start [this ^Stage stage]
  (let [view (WebView.)
        anyware (Anyware. view)]
    (swap! core/editor with-meta {:stage stage})
    (doto stage
      (.setTitle "Anyware")
      (.setScene (doto (Scene. view)
                   (.setOnKeyPressed (reify EventHandler
                                       (handle [this event]
                                         (core/run anyware event))))))
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
