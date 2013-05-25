(ns anyware.jvm
  (:require [clojure.set :as set]
            [clojure.java.io :as io]
            [anyware.core :as core]
            [anyware.core.api :as api]
            [anyware.core.html :as html]
            [anyware.core.tree :as tree]
            [anyware.jvm.file :as file]
            [anyware.jvm.clojure :as clj]
            [anyware.jvm.twitter :as twitter])
  (:gen-class
   :extends javafx.application.Application)
  (:import [javafx.application Application Platform]
           [javafx.event EventHandler]
           [javafx.stage Stage]
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
   KeyCode/BACK_SPACE :backspace
   KeyCode/ENTER :enter})

(defn keycode [^KeyEvent event]
  (let [keys (set/select (complement nil?)
                         (hash-set (if (.isControlDown event) :ctrl)
                                   (if (.isAltDown event) :alt)
                                   (-> event .getText first)))]
    (get special (.getCode event)
         (if (-> keys count (= 1))
           (first keys)
           keys))))

(extend-type anyware.core.editor.Editor
  core/Anyware
  (keycode [editor event] (keycode event))
  (render [editor]
    (-> editor meta :engine (.loadContent (html/render editor))))
  (quit [editor] (Platform/exit)))

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
    (swap! core/editor
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
