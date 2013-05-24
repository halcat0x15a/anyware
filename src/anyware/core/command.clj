(ns anyware.core.command
  (:require [anyware.core.api :as api]
            [anyware.core.keys :as keys]))

(declare commands vi)

(defn execute
  ([editor] (execute (api/command editor) editor))
  ([[f & args] editor]
     (if-let [f (commands f)]
       (-> (apply f editor args)
           (api/commit keys/command)
           (api/notice ""))
       editor)))

(def edit
  {:enter api/break
   :backspace api/backspace
   :left api/left
   :right api/right
   :up api/up
   :down api/down
   :default api/insert})

(defn minibuffer [keymap]
  {:escape #(assoc-in % keys/keymap keymap)
   :backspace (partial api/backspace keys/minibuffer)
   :right (partial api/right keys/minibuffer)
   :left (partial api/left keys/minibuffer)
   :enter execute
   :default (api/insert keys/minibuffer)})

(def default
  (->> {#{:ctrl \c} api/copy
        #{:ctrl \v} api/paste
        #{:ctrl \x} api/cut
        #{:ctrl \q} (partial execute "quit")
        #{:ctrl \o} (partial execute "open")
        #{:ctrl \s} (partial execute "save")
        #{:alt \m} #(assoc-in % keys/keymap minibuffer)}
       (merge edit)))
  
(def emacs
  (->> {#{:ctrl \f} api/right
        #{:ctrl \b} api/left
        #{:ctrl \n} api/down
        #{:ctrl \p} api/up
        #{:alt \f} api/right-word
        #{:alt \b} api/left-word
        #{:ctrl \e} api/end-of-line
        #{:ctrl \a} api/beginning-of-line
        #{:ctrl \m} api/break
        #{:ctrl \h} api/backspace
        #{:ctrl \d} api/delete
        #{:ctrl \k} api/delete-right
        #{:alt \m} #(assoc-in % keys/keymap (minibuffer emacs))}
       (merge edit)))

(def insert
  (->> {:escape #(-> % (assoc-in keys/keymap vi) api/commit)}
       (merge edit)))

(def delete
  {:escape #(-> % (assoc-in keys/keymap vi) api/commit)
   \l api/delete
   \h api/backspace
   \$ api/delete-right
   \^ api/delete-left
   \d api/delete-line
   \w api/delete-right-word
   \b api/delete-left-word
   :default (fn [editor key] editor)})

(def vi
  {:escape api/deselect
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
   #{:control \u} api/undo
   #{:control \r} api/redo
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
  {"next" api/next-buffer
   "prev" api/prev-buffer
   "new" api/open
   "vi" #(assoc-in % keys/keymap vi)
   "emacs" #(assoc-in % keys/keymap emacs)})
