(ns felis.root
  (:refer-clojure :exclude [find])
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [felis.style :as style]
            [felis.buffer :as buffer]
            [felis.node :as node]
            [felis.minibuffer :as minibuffer]
            [felis.default :as default]))

(defn add [buffer' {:keys [buffer buffers] :as root}]
  (assoc root
    :buffer buffer'
    :buffers (conj buffers buffer)))

(defn find [name {:keys [buffers]}]
  (->> buffers (filter (comp (partial = name) :name)) first))

(defn render [{:keys [buffer minibuffer style]}]
  (node/tag :html {}
            (node/tag :head {}
                      (node/tag :style {:type "text/css"}
                                "<!-- "
                                (style/css style)
                                " -->"))
            (node/tag :body {}
                      (node/tag :div {:class :editor}
                                (node/render buffer)
                                (node/render minibuffer)))))

(defrecord Root [buffer buffers minibuffer style]
  node/Node
  (render [root] (render root)))

(def path [:root])

(defn update [update editor]
  (update-in editor path update))

(def default
  (Root. buffer/default #{} minibuffer/default style/default))

(defmethod node/path Root [_] path)

(defmethod default/default Root [_] default)
