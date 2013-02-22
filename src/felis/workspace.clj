(ns felis.workspace
  (:refer-clojure :exclude [name])
  (:require [felis.buffer :as buffer]
            [felis.language :as language]
            [felis.history :as history]))

(defrecord Workspace [name buffer history language])

(def default
  (Workspace. :*scratch* buffer/default history/default language/text))
