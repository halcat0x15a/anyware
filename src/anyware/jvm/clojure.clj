(ns anyware.jvm.clojure
  (:refer-clojure :exclude [eval])
  (:require [clojure.string :as string]
            [clojure.tools.nrepl :as nrepl]
            [anyware.core.api :as api]
            [anyware.core.command :as command]
            [anyware.core.keys :as keys]
            [anyware.core.buffer :as buffer]))

(def timeout (atom 3000))

(defn nrepl [editor port]
  (vary-meta editor
    assoc ::nrepl
    (nrepl/client
     (->> port Integer/parseInt (nrepl/connect :port)) @timeout)))

(defn eval [editor & messages]
  (api/notice
   editor
   (-> editor meta ::nrepl
       (nrepl/message {:op :eval :code (string/join \space messages)})
       nrepl/response-values
       str)))

(defn init []
  (doto command/commands
    (swap! assoc "nrepl" nrepl)
    (swap! assoc "eval" eval)))
