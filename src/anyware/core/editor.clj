(ns anyware.core.editor
  (:require [anyware.core.buffer :as buffer]
            [anyware.core.canvas :as canvas]
            [anyware.core.parser :as parser]
            [anyware.core.clojure :as clojure]))

(declare normal)

(def ^:dynamic *buffers*
  {:*scratch* buffer/empty
   :minibuffer buffer/empty})
(def ^:dynamic *keymap*)
(def ^:dynamic *current* :*scratch*)

(defn update [f & args]
  (set! *buffers* (update-in *buffers* [*current*] #(apply f % args))))

(def delete
  {\h #(update buffer/delete -1)
   \l #(update buffer/delete 1)
   \w #(update buffer/delete :right buffer/word)
   \b #(update buffer/delete :left buffer/word)
   \$ #(update buffer/delete :right buffer/line)
   \^ #(update buffer/delete :left buffer/line)
   \tab #(set! *keymap* normal)})

(def insert
  {\backspace #(update buffer/delete -1)
   \tab #(set! *keymap* normal)
   :default #(update buffer/insert :left %)})

(def minibuffer insert)

(def normal
  {\h #(update buffer/move -1)
   \j (fn []
        (update #(-> %
                     (buffer/move :right buffer/line)
                     (buffer/move 1))))
   \k (fn []
        (update #(-> %
                     (buffer/move :left buffer/line)
                     (buffer/move -1))))
   \l #(update buffer/move 1)
   \w #(update buffer/move :right buffer/word)
   \b #(update buffer/move :left buffer/word)
   \$ #(update buffer/move :right buffer/line)
   \^ #(update buffer/move :left buffer/line)
   \v #(update buffer/select)
   \x #(update buffer/delete 1)
   \X #(update buffer/delete -1)
   \i #(set! *keymap* insert)
   \I (fn []
        (update buffer/move :left buffer/line)
        (set! *keymap* insert))
   \a (fn []
        (update buffer/move 1)
        (set! *keymap* insert))
   \A (fn []
        (update buffer/move :right buffer/line)
        (set! *keymap* insert))
   \o (fn []
        (update #(-> %
                     (buffer/move :right buffer/line)
                     (buffer/insert :left \newline)))
        (set! *keymap* insert))
   \O (fn []
        (update #(-> %
                     (buffer/move :left buffer/line)
                     (buffer/insert :left \newline)))
        (set! *keymap* insert))
   \d #(set! *keymap* delete)
   \tab #(set! *keymap* minibuffer)})

(defn run [input]
  (if-let [f (*keymap* input)] (f) (some-> *keymap* :default (#(% input))))
  (let [{:keys [buffer cursor]} (-> *buffers* *current* buffer/view)
        {:keys [value next]} (clojure/expression buffer)]
    (binding [canvas/*display* (object-array (-> buffer count inc))]
      (if-not (neg? cursor) (aset canvas/*display* cursor (canvas/->Color "white" "black")))
      (str (canvas/render canvas/html (str buffer \space)) next))))
