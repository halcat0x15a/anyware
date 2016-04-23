(ns anyware.main
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application]
           [javafx.scene Scene]
           [javafx.scene.web WebView]
           [javafx.stage Stage]))

(defn -start [this ^Stage stage]
  (let [view (WebView.)
        scene (Scene. view)]
    (doto stage
      (.setTitle "Anyware")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch anyware.main args))
