(ns felis.root
  (:refer-clojure :exclude [remove find])
  (:require [clojure.string :as string]
            [felis.html :as html]
            [felis.lisp.environment :as environment]
            [felis.workspace :as workspace]
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
  (html/write
   (html/->Node
    :html {}
    [(html/->Node
      :head {}
      (html/->Node
       :style {:type "text/css"}
       (str "<!-- " (html/css style) " -->")))
     (html/->Node
      :body {}
      (html/->Node
       :div {:class :editor}
       [(workspace/render workspace)
        (html/->Node
         :pre {:class :minibuffer}
         (text/render minibuffer))]))])))

(defrecord Root
    [workspace workspaces minibuffer environment style settings])

(def path [:root])

(def default
  (Root. workspace/default
         #{}
         text/default
         environment/global
         html/style
         {}))
