(ns anyware.core.keymap
  (:require [anyware.core.api :as api]))

(def default
  {#{:ctrl \c} api/copy
   #{:ctrl \v} api/paste
   :default api/insert})
