(ns anyware.core.format
  (:require [anyware.core.record :as record]
            [anyware.core.buffer :as buffer]
            [anyware.core.language.ast :as ast])
  (:import anyware.core.language.ast.Node))

(defprotocol Format
  (root [format child])
  (node [format node]))

(defn write [format node]
  (cond (instance? Node node) (node format node)
        (vector? node) (reduce str (mapv (partial write format) node))
        :else (str node)))

(defn render [format editor]
  (root format
        (str (->> editor (record/get record/buffer) ast/parse (write format))
             (->> editor (record/get record/minibuffer) buffer/write))))
