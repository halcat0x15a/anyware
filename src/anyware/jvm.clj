(ns anyware.jvm
  (:require [clojure.java.io :refer (as-file)]
            [anyware.core.buffer :as buffer]
            [anyware.core.workspace :as workspace]
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

(defmethod editor/command "open" [editor _ name]
  (update-in editor [:workspace] workspace/open (assoc (editor/window name) :buffer (buffer/buffer (slurp name)))))
(defmethod editor/command "write" [editor _]
  (let [{:keys [name buffer]} (get-in editor [:workspace :current])]
    (spit name (str buffer))
    editor))
(defmethod editor/command "exit" [editor _] (Platform/exit))

(defn -start [this ^Stage stage]
  (let [editor (atom editor/editor)
        view (doto (WebView.)
               (.setContextMenuEnabled false))
        handler (reify EventHandler
                  (handle [this event]
                    (time (->> event
                               .getText
                               first
                               (get special (.getCode event))
                               (swap! editor editor/run)))
                    (-> view
                        .getEngine
                        (.loadContent (str "<pre style=\"font-size: 16;\">" (time (editor/render @editor)) "</pre>")))))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (doto stage
      (.setTitle "Anyware")
      (.setScene scene)
      (.setOnShown (reify EventHandler
                     (handle [this event]
                       (swap! editor assoc-in [:view :height] (int (/ (.getHeight stage) 16))))))
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
