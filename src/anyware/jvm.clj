(ns anyware.jvm
  (:require [clojure.set :as set]
            [clojure.java.io :as io]
            [anyware.core :as core]
            [anyware.core.api :as api]
            [anyware.core.html :as html]
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

(def opacity (atom 1.0))

(def special
  {KeyCode/ESCAPE :escape
   KeyCode/LEFT :left
   KeyCode/RIGHT :right
   KeyCode/UP :up
   KeyCode/DOWN :down
   KeyCode/BACK_SPACE :backspace
   KeyCode/ENTER :enter})

(defn keycode [^KeyEvent event]
  (let [code (.getCode event)
        keys (set/select (complement nil?)
                         (hash-set (if (.isControlDown event) :ctrl)
                                   (if (.isAltDown event) :alt)
                                   (-> event .getText first)))
        key (if (= (count keys) 1) (first keys) keys)]
    (get special (.getCode event) key)))

(defrecord Anyware [^WebEngine engine]
  core/Anyware
  (keycode [this event] (keycode event))
  (render [this editor]
    (.loadContent engine (html/render editor))))

(def commands
  {"quit" (fn [_] (Platform/exit))
   "open" file/open
   "save" file/save
   "twitter" twitter/request
   "eval" clj/eval-file})

(def rc ".anyware")

(defn load-rc []
  (let [file (io/as-file (str (System/getProperty "user.home")
                              (System/getProperty "file.separator")
                              rc))]
    (if (.exists file)
      (load-file (.getPath file)))))

(defn -start [this ^Stage stage]
  (let [view (doto (WebView.)
               (.setContextMenuEnabled false))
        anyware (-> view .getEngine Anyware.)
        handler (reify EventHandler
                  (handle [this event]
                    (core/run anyware event)))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (core/init)
    (swap! core/editor with-meta {:stage stage})
    (swap! api/commands merge commands)
    (load-rc)
    (doto stage
      (.setTitle "Anyware")
      (.setOpacity @opacity)
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (prn args)
  (Application/launch anyware.jvm args))
