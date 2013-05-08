(ns anyware.core.keymap
  (:require [anyware.core.api :as api]
            [anyware.core.minibuffer :as minibuffer]))

(def default
  {:enter api/break
   :backspace api/backspace
   #{:ctrl \c} api/copy
   #{:ctrl \v} api/paste
   #{:alt \m} minibuffer/mode
   :default api/insert})
