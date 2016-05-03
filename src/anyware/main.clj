(ns anyware.main
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application]
           [javafx.event EventHandler]
           [javafx.scene Scene]
           [javafx.scene.input KeyCode KeyEvent]
           [javafx.scene.web WebView]
           [javafx.stage Stage])
  (:require [anyware.editor :as editor]
            [anyware.html :as html]))

(def editor (atom editor/default))

(defn handle-key [^KeyEvent event]
  (let [^KeyCode code (.getCode event)
        key (condp identical? code
              KeyCode/ESCAPE :escape
              KeyCode/LEFT :left
              KeyCode/RIGHT :right
              KeyCode/UP :up
              KeyCode/DOWN :down
              KeyCode/ENTER :enter
              KeyCode/BACK_SPACE :backspace
              (.getText event))]
    (reset! editor (editor/run key @editor))))

(defn -start [this ^Stage stage]
  (eval '(require '[anyware.command :refer :all]))
  (let [view (WebView.)
        engine (.getEngine view)
        scene (Scene. view)]
    (.setOnKeyPressed scene (reify EventHandler
                              (handle [this event]
                                (handle-key event)
                                (.loadContent engine (html/render (editor/html @editor))))))
    (doto stage
      (.setTitle "Anyware")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch anyware.main args))
