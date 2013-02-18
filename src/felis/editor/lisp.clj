(ns felis.editor.lisp
  (:refer-clojure :exclude [eval])
  (:require [felis.root :as root])
  (:require [felis.buffer :as buffer]))

(defn eval [editor]
  (lisp/read-string (get-in editor root/environment)
                    (get-in editor buffer/path)))
