(ns felis.main
  (:require [felis.key :as key]
            [felis.editor :as editor]
            [felis.editor.normal :as normal]))

(defn run [editor keycode event]
  (let [key (editor/code keycode event)]
    (prn key)
    (if-let [update
             (-> {key/escape identity}
                 (merge (editor/keymap editor))
                 (update-in [key/escape]
                            (partial comp normal/map->Normal))
                 (get key))]
      (update editor)
      (editor/input editor key))))
