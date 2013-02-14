(ns felis.editor.history
  (:require [felis.history :as history]
            [felis.buffer :as buffer]))

(defn commit [editor]
  (update-in
   editor
   history/path
   (partial history/commit
            (get-in editor buffer/path))))

(defn undo [editor]
  (if-let [history
           (-> editor (get-in history/path) history/undo)]
    (-> editor
        (assoc-in history/path history)
        (assoc-in buffer/path (:present history)))
    editor))
