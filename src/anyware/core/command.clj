(ns anyware.core.command
  (:require [anyware.core.api :as api]
            [anyware.core.file :as file]
            [anyware.core.keys :as keys]))

(declare commands vi)

(defn execute
  ([editor] (execute (api/command editor) editor))
  ([[f & args] editor]
     (if-let [f (@commands f)]
       (-> (apply f editor args)
           (api/commit keys/command))
       editor)))

(def edit
  {:left api/left
   :right api/right
   :up api/up
   :down api/down
   #{:shift :left} (comp api/left api/selecting)
   #{:shift :right} (comp api/right api/selecting)
   #{:shift :up} (comp api/up api/selecting)
   #{:shift :down} (comp api/down api/selecting)
   \newline api/break
   \backspace api/backspace
   :default api/insert})

(defn minibuffer [keymap]
  {:esc #(assoc-in % keys/keymap keymap)
   :right (partial api/right keys/minibuffer)
   :left (partial api/left keys/minibuffer)
   \newline execute
   \backspace (partial api/backspace keys/minibuffer)
   :default (api/insert keys/minibuffer)})

(def default
  (->> {#{:ctrl \C} api/copy
        #{:ctrl \V} api/paste
        #{:ctrl \X} api/cut
        #{:ctrl \Z} api/undo
        #{:ctrl :shift \Z} api/redo
        #{:ctrl \Q} (partial execute ["quit"])
        #{:ctrl \O} (partial execute ["open"])
        #{:ctrl \S} (partial execute ["save"])
        #{:alt \M} #(assoc-in % keys/keymap (minibuffer default))}
       (merge edit)))
  
(def emacs
  (->> {#{:ctrl \F} api/right
        #{:ctrl \B} api/left
        #{:ctrl \N} api/down
        #{:ctrl \P} api/up
        #{:alt \F} api/right-word
        #{:alt \B} api/left-word
        #{:ctrl \E} api/end-of-line
        #{:ctrl \A} api/beginning-of-line
        #{:ctrl \M} api/break
        #{:ctrl \H} api/backspace
        #{:ctrl \D} api/delete
        #{:ctrl \K} api/delete-right
        #{:alt \M} #(assoc-in % keys/keymap (minibuffer emacs))}
       (merge edit)))

(def insert
  (->> {:esc #(-> % (assoc-in keys/keymap vi) api/commit)}
       (merge edit)))

(def delete
  {:esc #(-> % (assoc-in keys/keymap vi) api/commit)
   \l api/delete
   \h api/backspace
   \$ api/delete-right
   \^ api/delete-left
   \d api/delete-line
   \w api/delete-right-word
   \b api/delete-left-word
   :default (fn [editor key] editor)})

(def vi
  {:esc api/deselect
   \l api/right
   \h api/left
   \j api/down
   \k api/up
   \w api/right-word
   \b api/left-word
   \$ api/end-of-line
   \^ api/beginning-of-line
   \v api/select
   \y api/copy
   \x api/delete
   \X api/backspace
   #{:ctrl \U} api/undo
   #{:ctrl \R} api/redo
   \i #(assoc-in % keys/keymap insert)
   \I #(-> % api/beginning-of-line (assoc-in keys/keymap insert))
   \a #(-> % api/right (assoc-in keys/keymap insert))
   \A #(-> % api/end-of-line (assoc-in keys/keymap insert))
   \o #(-> % api/end-of-line api/break (assoc-in keys/keymap insert))
   \O #(-> % api/beginning-of-line api/break api/left (assoc-in keys/keymap insert))
   \d #(assoc-in % keys/keymap delete)
   \: #(assoc-in % keys/keymap (minibuffer vi))
   :default (fn [editor key] editor)})

(def commands
  (atom {"next" api/next-buffer
         "prev" api/prev-buffer
         "new" api/open
         "vi" #(assoc-in % keys/keymap vi)
         "emacs" #(assoc-in % keys/keymap emacs)
         "open" file/open
         "save" file/save}))
