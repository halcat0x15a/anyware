(ns anyware.jvm
  (:require [anyware.core :as core]
            [anyware.core.format :as format]
            [anyware.core.format.html :as html]
            [anyware.jvm.file :as file])
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application Platform]
           [javafx.event EventHandler]
           [javafx.stage Stage]
           [javafx.scene Scene]
           [javafx.scene.web WebView WebEngine]
           [javafx.scene.input KeyCode KeyEvent]))

(def special
  {KeyCode/ESCAPE :escape
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/BACK_SPACE :backspace
   KeyCode/ENTER :enter})

(defn keycode [^KeyEvent event]
  (get special (.getCode event) (first (.getText event))))

(defrecord Anyware [^WebEngine engine]
  core/Anyware
  (keycode [this event] (keycode event))
  (render [this editor]
    (.loadContent engine (format/render html/format editor))))

(def command
  {"quit" (fn [_] (Platform/exit))
   "open" file/open
   "save" file/save})

(defn -start [this ^Stage stage]
  (let [view (WebView.)
        anyware (-> view .getEngine Anyware.)
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
