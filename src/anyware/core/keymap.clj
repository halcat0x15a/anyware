(ns anyware.core.keymap
  (:require [anyware.core.api :as api]
            [anyware.core.minibuffer :as minibuffer]))

(def global
  {:enter api/break
   :backspace api/backspace
   :left api/left
   :right api/right
   :up api/up
   :down api/down
   :default api/insert})

(declare default)

(def minibuffer #(minibuffer/mode % default))

(def default
  (merge global
         {#{:ctrl \c} api/copy
          #{:ctrl \v} api/paste
          #{:ctrl \x} api/cut
          #{:ctrl \q} (api/execute "quit")
          #{:ctrl \o} (api/execute "open")
          #{:ctrl \s} (api/execute "save")
          #{:alt \m} minibuffer}))
