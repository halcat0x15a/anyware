(ns felis.main
  (:require [felis.key :as key]
            [felis.editor :as editor]
            [felis.editor.normal :as normal]))

(def global
  { })

(defn run [editor keycode event]
  (let [key (editor/code keycode event)]
    (if-let [update
             (-> editor
                 editor/keymap
                 (update-in [key/escape] (partial comp normal/map->Normal))
                 (get key))]
      (update editor)
      (editor/input editor key))))
