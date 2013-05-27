(ns anyware.jvm
  (:require [clojure.set :as set]
            [clojure.java.io :as io]
            [anyware.core :as core]
            [anyware.core.api :as api]
            [anyware.core.html :as html]
            [anyware.core.tree :as tree]
            [anyware.core.file :as file]
            [anyware.jvm.clojure :as clj]
            [anyware.jvm.twitter :as twitter])
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application Platform]
           [javafx.event EventHandler]
           [javafx.stage Stage FileChooser]
           [javafx.scene Scene]
           [javafx.scene.web WebView WebEngine]
           [javafx.scene.input KeyCode KeyEvent]))

(def home (System/getProperty "user.home"))

(def separator (System/getProperty "file.separator"))

(def opacity (atom 1.0))

(def rc (atom ".anyware"))

(def special
  {KeyCode/ESCAPE :escape
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/ENTER \newline
   KeyCode/BACK_SPACE \backspace})

(extend-type KeyEvent
  core/Event
  (alt? [event] (.isAltDown event))
  (ctrl? [event] (.isControlDown event))
  (keycode [event]
    (let [code (.getCode event)]
      (prn (.isLetterKey code) (-> code .getName first))
      (if (.isLetterKey code) (-> code .getName first))))
  (keychar [event]
    (get special (.getCode event) (-> event .getText first))))

(def ^FileChooser chooser (FileChooser.))

(extend-type anyware.core.editor.Editor
  core/Anyware
  (render [editor]
    (-> editor meta :engine (.loadContent (html/render editor))))
  (quit [editor] (Platform/exit))
  file/IO
  (dialog [editor]
    (if-let [file (.showOpenDialog chooser (-> editor meta :stage))]
      (file/->File (.getPath file) (slurp file))))
  (read [editor path]
    (file/->File path (slurp path)))
  (write [editor path value]
    (spit path value)
    editor))

(defn load-rc []
  (let [file (io/file (str home separator @rc))]
    (when (.exists file)
      (-> file io/reader load-reader))))

(defn -start [this ^Stage stage]
  (let [view (doto (WebView.)
               (.setContextMenuEnabled false))
        scene (doto (Scene. view)
                (.setOnKeyPressed
                 (reify EventHandler
                   (handle [this event]
                     (core/run! event)))))]
    (swap! core/reference
           with-meta
           {:stage stage
            :engine (.getEngine view)})
    (load-rc)
    (doto stage
      (.setTitle "Anyware")
      (.setOpacity @opacity)
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
