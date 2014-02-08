(ns anyware.core.editor
  (:refer-clojure :exclude [read])
  (:require [clojure.string :as string]
            [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.view :as view]
            [anyware.core.workspace :as workspace]
            [anyware.core.canvas :as canvas]
            [anyware.core.parser :as parser]
            [anyware.core.clojure :as clojure]
            [anyware.core.util :as util]))

(declare normal)

(defrecord Window [name buffer history parser]
  workspace/Window
  (id [window] name))

(defn window [name]
  (Window. name buffer/empty (history/history buffer/empty) identity))

(defrecord Editor [workspace minibuffer view keymap clipboard]
  Object
  (toString [this] 
    (-> workspace :current :buffer str)))

(def buffer [:workspace :current :buffer])
(def history [:workspace :current :history])
(def minibuffer [:minibuffer :buffer])

(defmulti command (fn [editor name & args] name))
(defmethod command "new" [editor _ name]
  (update-in editor [:workspace] workspace/open (window name)))
(defmethod command "buffer" [editor _ name]
  (update-in editor [:workspace] workspace/select name))
(defmethod command :default [editor _] nil)

(defn keymap [editor input] input)

(defmulti insert keymap)
(defmethod insert \backspace [editor _]
  (update-in editor buffer buffer/delete -1))
(defmethod insert :esc [editor _]
  (-> editor
      (update-in history history/commit (get-in editor buffer))
      (assoc :keymap normal)))
(defmethod insert :default [editor input]
  (update-in editor buffer buffer/insert :left input))

(defmulti delete keymap)
(defmethod delete \h [editor _] (insert editor \backspace))
(defmethod delete \l [editor _]
  (update-in editor buffer buffer/delete 1))
(defmethod delete \w [editor _]
  (update-in editor buffer buffer/delete-matches :right buffer/word))
(defmethod delete \b [editor _]
  (update-in editor buffer buffer/delete-matches :left buffer/word))
(defmethod delete \$ [editor _]
  (update-in editor buffer buffer/delete-matches :right buffer/line))
(defmethod delete \^ [editor _]
  (update-in editor buffer buffer/delete-matches :left buffer/line))
(defmethod delete :esc [editor input] (insert editor input))
(defmethod delete :default [editor _] editor)

(defmulti command-line keymap)
(defmethod command-line \backspace [editor _]
  (update-in editor minibuffer buffer/delete -1))
(defmethod command-line \newline [editor _]
  (let [[f & args] (-> editor (get-in minibuffer) str (string/split #"\s"))]
    (or (some-> (apply command editor f args)
                (update-in [:minibuffer :history] history/commit (get-in editor minibuffer))
                (assoc-in minibuffer buffer/empty))
        editor)))
(defmethod command-line :esc [editor _]
  (assoc editor :keymap normal))
(defmethod command-line :default [editor input]
  (update-in editor minibuffer buffer/insert :left input))

(defmulti normal keymap)
(defmethod normal \h [editor _]
  (update-in editor buffer buffer/move :left buffer/character))
(defmethod normal \j [editor _]
  (-> editor (normal \$) (normal \l)))
(defmethod normal \k [editor _]
  (-> editor (normal \^) (normal \h) (normal \^)))
(defmethod normal \l [editor _]
  (update-in editor buffer buffer/move :right buffer/character))
(defmethod normal \w [editor _]
  (update-in editor buffer buffer/move :right buffer/word))
(defmethod normal \b [editor _]
  (update-in editor buffer buffer/move :left buffer/word))
(defmethod normal \$ [editor _]
  (update-in editor buffer buffer/move :right buffer/line))
(defmethod normal \^ [editor _]
  (update-in editor buffer buffer/move :left buffer/line))
(defmethod normal \v [editor _]
  (update-in editor buffer buffer/select))
(defmethod normal \y [editor _]
  (update-in editor [:clipboard] history/commit (-> editor :workspace :current :value buffer/copy)))
(defmethod normal \p [{:keys [clipboard] :as editor} _]
  (update-in buffer buffer/insert :left (history/present clipboard)))
(defmethod normal \u [editor _]
  (-> editor
      (update-in history history/commit (get-in editor buffer))
      (update-in history history/undo)
      (assoc-in buffer (-> editor (get-in history) history/present))))
(defmethod normal \x [editor _]
  (update-in editor buffer buffer/delete 1))
(defmethod normal \X [editor _]
  (update-in editor buffer buffer/delete -1))
(defmethod normal \i [editor _]
  (assoc editor :keymap insert))
(defmethod normal \I [editor _]
  (-> editor (normal \^) (normal \i)))
(defmethod normal \a [editor _]
  (-> editor (normal \l) (normal \i)))
(defmethod normal \A [editor _]
  (-> editor (normal \$) (normal \i)))
(defmethod normal \o [editor _]
  (-> editor (normal \$) (insert \newline) (normal \i)))
(defmethod normal \O [editor _]
  (-> editor (normal \^) (insert \newline) (normal \i)))
(defmethod normal \d [editor _]
  (assoc editor :keymap delete))
(defmethod normal \: [editor _]
  (assoc editor :keymap command-line))
(defmethod normal :default [editor _] editor)

(defn run [{:keys [keymap] :as editor} input]
  (let [editor (keymap editor input)]
    (update-in editor [:view] view/move (-> editor (get-in buffer) :left util/split-lines count dec))))

(defn render [{:keys [minibuffer view] :as editor}]
  (let [{:keys [left] :as buffer} (get-in editor buffer)
        {:keys [y height]} view]
    (str (canvas/render (:buffer minibuffer) 0 1)
         \newline
         (canvas/render buffer y height))))

(def editor
  (Editor.
   (workspace/workspace (window :*scratch*))
   (window :*minibuffer*)
   (view/->View 0 0 80 24)
   normal
   (history/history "")))
