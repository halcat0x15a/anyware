(ns anyware.jvm
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.window :as window]
            [anyware.core.editor :as editor]
            [anyware.jvm.io :as io])
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application Platform]
           [javafx.event EventHandler]
           [javafx.stage Stage]
           [javafx.scene Scene]
           [javafx.scene.web WebView WebEngine]
           [javafx.scene.input KeyCode KeyEvent]))

(def special
  {KeyCode/TAB \tab
   KeyCode/ENTER \newline
   KeyCode/BACK_SPACE \backspace
   KeyCode/ESCAPE :esc})

(def command
  {"open" io/open
   "write" io/write
   "exit" (fn [_] (Platform/exit))})

(defn -start [this ^Stage stage]
  (let [editor (atom editor/editor)
        view (doto (WebView.)
               (.setContextMenuEnabled false))
        handler (reify EventHandler
                  (handle [this event]
                    (binding [editor/*command* (merge editor/*command* command)]
                      (time (->> event
                           .getText
                           first
                           (get special (.getCode event))
                           (swap! editor editor/run)))
                      (-> view
                          .getEngine
                          (.loadContent (str "<pre style=\"font-size: 16;\">" (time (editor/render @editor)) "</pre>"))))))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (doto stage
      (.setTitle "Anyware")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
