(ns anyware.core.format
  (:require [anyware.core.api :refer [buffer minibuffer]]
            [anyware.core.buffer :as buffer]
            [anyware.core.language.ast :as ast]))

(defprotocol Format
  (root [format child])
  (node [format node])
  (text [format text]))

(defn write [format x]
  (cond (:label x) (node format x)
        (vector? x) (reduce str (mapv (partial write format) x))
        :else (text format (str x))))

(defn render [format editor]
  (root format
        (str (->> (get-in editor minibuffer) buffer/write (text format))
             \newline
             (->> (get-in editor buffer) ast/parse (write format)))))
