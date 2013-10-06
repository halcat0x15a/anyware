(ns anyware.jvm
  (:require [clojure.pprint :refer (pprint)]
            [clojure.core.async :refer (chan <!! >! go)]
            [anyware.core.editor :as editor]
            [anyware.core.buffer :as buffer])
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
   KeyCode/BACK_SPACE \backspace})

(def input (chan))

(defn -stop [this]
  (go (>! input false))
  (shutdown-agents))

(defn -start [this ^Stage stage]
  (let [view (doto (WebView.)
               (.setContextMenuEnabled false))
        handler (reify EventHandler
                  (handle [this event]
                    (go (some->> event .getText first
                                 (get special (.getCode event))
                                 (>! input)))))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (future
      (try
        (binding [editor/*buffers* editor/*buffers*
                  editor/*keymap* editor/normal]
          (loop []
            (when-let [char (<!! input)]
              (let [html (str "<pre>" (editor/run char) "</pre>")]
                (Platform/runLater #(-> view .getEngine (.loadContent html)))
                (recur)))))
          (catch Exception e
            (.printStackTrace e))))
    (doto stage
      (.setTitle "Anyware")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
