(ns felis.root
  (:refer-clojure :exclude [remove find])
  (:require [clojure.string :as string]
            [clojure.walk :as walk]
            [felis.style :as style]
            [felis.lisp.environment :as environment]
            [felis.buffer :as buffer]
            [felis.node :as node]
            [felis.minibuffer :as minibuffer]
            [felis.default :as default]))

(defn add [buffer' {:keys [buffer buffers] :as root}]
  (assoc root
    :buffer buffer'
    :buffers (conj buffers buffer)))

(defn remove [{:keys [buffer buffers] :as root}]
  (if-let [buffer' (first buffers)]
    (assoc root
      :buffer buffer'
      :buffers (disj buffers buffer'))
    root))

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

(defrecord Root [buffer buffers minibuffer environment style settings]
  node/Node
  (render [root] (render root)))

(def path [:root])

(def default
  (Root. buffer/default
         #{}
         minibuffer/default
         environment/global
         style/default
         {}))

(defmethod node/path Root [_] path)

(defmethod default/default Root [_] default)
