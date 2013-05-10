(ns anyware.core.keymap
  (:require [anyware.core.api :as api]
            [anyware.core.minibuffer :as minibuffer]))

(def default
  {:enter api/break
   :backspace api/backspace
   :left api/left
   :right api/right
   :up api/up
   :down api/down
   #{:ctrl \c} api/copy
   #{:ctrl \v} api/paste
   #{:ctrl \x} api/cut
   #{:alt \m} minibuffer/mode
   :default api/insert})
