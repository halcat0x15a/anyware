(ns felis.editor.history
  (:require [felis.path :as path]
            [felis.history :as history]))

(defn commit [editor]
  (update-in editor
             path/history
             (partial history/commit
                      (get-in editor path/buffer))))

(defn undo [editor]
  (if-let [history
           (-> editor (get-in path/history) history/undo)]
    (-> editor
        (assoc-in path/history history)
        (assoc-in path/buffer (:present history)))
    editor))
