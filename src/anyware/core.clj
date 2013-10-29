(ns anyware.core
  (:require [clojure.set :as set]
            [anyware.core.editor :as editor]
            [anyware.core.keys :as keys]
            [anyware.core.api :as api])
  (:import
;*CLJSBUILD-REMOVE*;cljs.core.ExceptionInfo #_
   clojure.lang.ExceptionInfo))

(def reference (atom editor/default :validator keys/validate))

(defprotocol Anyware
  (render [this])
  (quit [this]))

(defprotocol Event
  (alt? [event])
  (ctrl? [event])
  (keycode [event])
  (keychar [event]))

(defn keyset [event]
  (let [keys (->> {:alt (alt? event)
                   :ctrl (ctrl? event)}
                  (filter val)
                  (map key)
                  (apply hash-set (keycode event)))]
    (if (-> keys count dec pos?) keys (keychar event))))

(defn run [editor event]
  (try
    (api/run editor (keyset event))
    (catch ExceptionInfo e
      (api/notice editor (str e)))))

(defn run! [event]
  (-> reference (swap! run event) render))
