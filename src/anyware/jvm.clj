(ns anyware.jvm
  (:require [anyware.core :as core]
            [anyware.core.command :as command]
            [anyware.core.format.html :as html]
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
      (get special (.getCode event) (-> event .getText first))))
  (format [this] html/format)
  (render [this string]
    (.. view getEngine (loadContent string))))

(defmethod command/exec "quit" [_ _] (Platform/exit))

(defn -start [this ^Stage stage]
  (let [view (WebView.)
        anyware (Anyware. view)
        handler (reify EventHandler
                  (handle [this event]
                    (core/run anyware event)))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (swap! core/editor with-meta {:stage stage})
    (doto stage
      (.setTitle "Anyware")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
