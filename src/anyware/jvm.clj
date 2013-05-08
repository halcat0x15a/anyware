(ns anyware.jvm
  (:require [clojure.set :as set]
            [anyware.core :as core]
            [anyware.core.api :as api]
            [anyware.core.format :as format]
            [anyware.core.format.html :as html]
            [anyware.jvm.file :as file]
            [anyware.jvm.twitter :as twitter])
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
   KeyCode/ENTER :enter
   KeyCode/ALT :alt
   KeyCode/CONTROL :ctrl})

(defn keycode [^KeyEvent event]
  (let [modifiers (set/select (complement nil?)
                              (hash-set (if (.isControlDown event) :ctrl)
                                        (if (.isAltDown event) :alt)))
        key (first (.getText event))
        keys (if (empty? modifiers) key (conj modifiers key))]
      (get special (.getCode event) keys)))

(defrecord Anyware [^WebEngine engine]
  core/Anyware
  (keycode [this event] (keycode event))
  (render [this editor]
    (.loadContent engine (format/render html/format editor))))

(def commands
  {"quit" (fn [_] (Platform/exit))
   "open" file/open
   "save" file/save
   "twitter" twitter/request})

(defn -start [this ^Stage stage]
  (let [view (WebView.)
        anyware (-> view .getEngine Anyware.)
        handler (reify EventHandler
                  (handle [this event]
                    (core/run anyware event)))
        scene (doto (Scene. view)
                (.setOnKeyPressed handler))]
    (swap! core/editor with-meta {:stage stage})
    (swap! api/commands merge commands)
    (doto stage
      (.setTitle "Anyware")
      (.setScene scene)
      (.show))))

(defn -main [& args]
  (Application/launch anyware.jvm args))
