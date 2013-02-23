(ns anyware.core
  (:require [anyware.editor :as editor]
            [anyware.keymap :as keymap]))

(defprotocol KeyCode
  (code [this event]))

(defn run
  ([editor keycode event] (run @keymap/keymap editor keycode event))
  ([keymap {:keys [mode] :as editor} keycode event]
     (let [key (code keycode event)]
       (if-let [update (-> keymap
                           (get mode)
                           :map
                           (merge {:escape #(assoc % :mode :normal)})
                           (get key))]
         (update editor)
         ((:input (get keymap mode)) editor key)))))
