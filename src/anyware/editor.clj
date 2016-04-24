(ns anyware.editor
  (:require [anyware.buffer :as buffer]))

(declare normal-mode insert-mode minibuffer-mode)

(defrecord Editor [current buffers mode style])

(defn get-buffer [editor]
  (get (:buffers editor) (:current editor)))

(defn update-buffer [f editor]
  (let [current (:current editor)
        buffers (:buffers editor)]
    (assoc editor :buffers (assoc buffers current (f (get buffers current))))))

(defn open [path editor]
  (assoc editor :buffers (assoc (:buffers editor) path (buffer/create (slurp path)))))

(defn run [value editor]
  (let [mode (:mode editor)]
    (if-let [f (get mode value)]
      (f editor)
      ((get mode :default) value editor))))

(defn eval-minibuffer [editor]
  ((eval (read-string (buffer/text (get (:buffers editor) "*minibuffer*")))) editor))

(defn insert [value field editor]
  (if (or (char? value) (string? value))
    (update-buffer #(buffer/insert value field %) editor)
    editor))

(defn delete [n field editor]
  (update-buffer #(buffer/delete n field %) editor))

(defn move [n field editor]
  (update-buffer #(buffer/move n field %) editor))

(defn move-line [field editor]
  (update-buffer #(buffer/move-line field %) editor))

(defn move-left [editor]
  (move 1 :left editor))

(defn move-right [editor]
  (move 1 :right editor))

(defn move-up [editor]
  (move-line :left editor))

(defn move-down [editor]
  (move-line :right editor))

(defn set-mode [mode editor]
  (assoc editor :mode mode))

(defn set-current [current editor]
  (assoc editor :current current))

(defn html [editor]
  (str "<html><head><style>" (:style editor) "</style></head>"
       "<body>"
       (buffer/html (get (:buffers editor) "*scratch*"))
       (buffer/html (get (:buffers editor) "*minibuffer*"))
       "</body></html>"))

(def normal-mode
  {"i" #(set-mode insert-mode %)
   "h" move-left
   "j" move-down
   "k" move-up
   "l" move-right
   ":" #(->> % (set-current "*minibuffer*") (set-mode minibuffer-mode))
   :left move-left
   :up move-up
   :right move-right
   :down move-down
   :default (fn [value editor] editor)})

(def insert-mode
  {:enter #(insert \newline :left %)
   :backspace #(delete 1 :left %)
   :left move-left
   :up move-up
   :right move-right
   :down move-down
   :escape #(set-mode normal-mode %)
   :default (fn [value editor] (insert value :left editor))})

(def minibuffer-mode (assoc insert-mode :enter eval-minibuffer))

(def default (Editor. "*scratch*" {"*scratch*" buffer/default "*minibuffer*" buffer/default} normal-mode ".cursor { color: white; background-color: black; }"))
