(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.history :as history]
            [anyware.core.frame :as frame]
            [anyware.core.record :as record]
            [anyware.core.parser :as parser]))

(defrecord Editor [frame minibuffer mode clipboard message])

(defn log [level message editor]
  (->> editor
       (record/modify :message
                      #(merge-with concat % {level [message]}))
       (record/set record/minibuffer (buffer/read message))))

(def buffer (atom "*scratch*"))

(def mode (atom :normal))

(def history
  (history/create (with-meta buffer/empty {:parser parser/id})))

(def frame (frame/create (frame/window @buffer history)))

(def default (Editor. frame history @mode (history/create "") {}))
