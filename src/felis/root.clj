(ns felis.root
  (:refer-clojure :exclude [remove find])
  (:require [clojure.string :as string]
            [felis.html :as html]
            [felis.lisp.environment :as environment]
            [felis.workspace :as workspace]
            [felis.edit :as edit]
            [felis.text :as text]))

(defn add [workspace' {:keys [workspace workspaces] :as root}]
  (assoc root
    :workspace workspace'
    :workspaces (conj workspaces workspace)))

(defn remove [{:keys [workspace workspaces] :as root}]
  (if-let [workspace' (first workspaces)]
    (assoc root
      :workspace workspace'
      :workspaces (disj workspaces workspace'))
    root))

(defn render [{:keys [workspace minibuffer style]}]
  [(html/< :head {}
           (html/< :style {:type "text/css"}
                   (-> style
                       (assoc-in [:.buffer :top]
                                 (-> (->> workspace :buffer (edit/cursor :tops)) (* -17) (str "px")))
                       html/css)))
   (html/< :body {}
           (html/< :div {:class :editor}
                   (workspace/render workspace)
                   (html/< :pre {:class :minibuffer}
                           (text/render minibuffer))))])

(defrecord Root [workspace
                 workspaces
                 minibuffer
                 environment
                 style
                 settings]
  html/Node
  (render [buffer] (render buffer)))

(def path [:root])

(def environment (conj path :environment))

(def default
  (Root. workspace/default
         #{}
         text/default
         environment/global
         html/style
         {}))
