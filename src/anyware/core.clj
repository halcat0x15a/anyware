(ns anyware.core
  (:require [anyware.core.editor :as editor]
            [anyware.core.keys :as keys]
            [anyware.core.api :as api])
  (:import clojure.lang.ExceptionInfo))

(def editor (atom editor/default :validator keys/validate))

(defprotocol Anyware
  (keycode [this event])
  (render [this])
  (quit [this]))

(defn run [editor event]
  (try
    (api/run editor (keycode editor event))
    (catch ExceptionInfo e (api/notice editor (str e)))))

(defn run! [event]
  (-> editor (swap! run event) render))
