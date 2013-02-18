(ns felis.workspace
  (:refer-clojure :exclude [name])
  (:require [felis.string :as string]
            ;*CLJSBUILD-REMOVE*;[cljs.core :as core]
            [felis.serialization :as serialization]
            [felis.html :as html]
            [felis.buffer :as buffer]
            [felis.text :as text]
            [felis.syntax :as syntax]
            [felis.history :as history]))

;*CLJSBUILD-REMOVE*;(comment
(require '[clojure.core :as core])
;*CLJSBUILD-REMOVE*;)

(defn render [{:keys [name buffer syntax]}]
  [(html/< :p {:class :status} (core/name name))
   (html/< :pre {:class :buffer}
           (->> buffer serialization/write (syntax/highlight syntax))
           (html/< :span {:class :cursor}
                   (->> (buffer/focus buffer)
                        (map text/cursor)
                        (interpose (str \newline))
                        vec)))])

(defrecord Workspace [name buffer history syntax]
  html/Node
  (render [workspace] (render workspace)))

(def default
  (Workspace. :*scratch* buffer/default history/default syntax/default))

(def path [:root :workspace])

(def name (conj path :name))
