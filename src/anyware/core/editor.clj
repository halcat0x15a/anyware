(ns anyware.core.editor
  (:refer-clojure :exclude [read])
  (:require [clojure.string :as string]
            [anyware.core.buffer :as buffer]
            [anyware.core.view :as view]
            [anyware.core.window :as window]
            [anyware.core.canvas :as canvas]
            [anyware.core.parser :as parser]
            [anyware.core.clojure :as clojure]))

(declare normal)

(def ^:dynamic *style*
  {:cursor (canvas/->Color "white" "black")
   :symbol (canvas/->Color "blue" "white")
   :string (canvas/->Color "maroon" "white")
   :keyword (canvas/->Color "aqua" "white")
   :special (canvas/->Color "magenta" "white")
   :default (canvas/->Color "black" "white")})

(defrecord Editor [buffers view keymap directory])

(defn update-buffer [editor f & args]
  (apply update-in editor [:buffers] window/edit f args))

(def ^:dynamic *command*
  {"new" (fn [editor name]
           (update-in editor [:buffers] window/open name buffer/empty))
   "cd" (fn [editor dir] (assoc editor :directory dir))})

(def default
  {:esc #(assoc % :keymap normal)})

(def delete
  {\h #(update-buffer % buffer/delete -1)
   \l #(update-buffer % buffer/delete 1)
   \w #(update-buffer % buffer/delete :right buffer/word)
   \b #(update-buffer % buffer/delete :left buffer/word)
   \$ #(update-buffer % buffer/delete :right buffer/line)
   \^ #(update-buffer % buffer/delete :left buffer/line)})

(def insert
  {\backspace #(update-buffer % buffer/delete -1)
   :default #(update-buffer %1 buffer/insert :left %2)})

(defn exec [editor]
  (let [[f & args] (-> editor
                       (get-in [:buffers :minibuffer])
                       buffer/show
                       (string/split #"\s"))]
    (if-let [f (*command* f)]
      (assoc-in (apply f editor args) [:buffers :minibuffer] buffer/empty)
      editor)))

(def minibuffer
  {\backspace #(update-in % [:buffers :minibuffer] buffer/delete -1)
   \newline exec
   :default #(update-in %1 [:buffers :minibuffer] buffer/insert :left %2)})

(def normal
  {\h #(update-buffer % buffer/move -1)
   \j #(-> %
           (update-buffer buffer/move :right buffer/line)
           (update-buffer buffer/move 1))
   \k #(-> %
           (update-buffer buffer/move :left buffer/line)
           (update-buffer buffer/move -1))
   \l #(update-buffer % buffer/move 1)
   \w #(update-buffer % buffer/move :right buffer/word)
   \b #(update-buffer % buffer/move :left buffer/word)
   \$ #(update-buffer % buffer/move :right buffer/line)
   \^ #(update-buffer % buffer/move :left buffer/line)
   \v #(update-buffer % buffer/select)
   \x #(update-buffer % buffer/delete 1)
   \X #(update-buffer % buffer/delete -1)
   \i #(assoc % :keymap insert)
   \I #(-> %
           (update-buffer buffer/move :left buffer/line)
           (assoc :keymap insert))
   \a #(-> %
           (update-buffer buffer/move 1)
           (assoc :keymap insert))
   \A #(-> %
           (update-buffer buffer/move :right buffer/line)
           (assoc :keymap insert))
   \o #(-> %
           (update-buffer buffer/move :right buffer/line)
           (update-buffer buffer/insert :left \newline)
           (assoc :keymap insert))
   \O #(-> %
           (update-buffer buffer/move :left buffer/line)
           (update-buffer buffer/insert :left \newline)
           (assoc :keymap insert))
   \d #(assoc % :keymap delete)
   \: #(assoc % :keymap minibuffer)})

(defn run [editor input]
  (let [editor (if-let [f ((merge default (:keymap editor)) input)]
                 (f editor)
                 (if-let [f (some-> editor :keymap :default)]
                   (f editor input)
                   editor))]
    (update-in editor [:view] view/move (-> editor :buffers window/current buffer/linage))))

(defn paint
  ([{:keys [buffer] :as view} current]
     (paint view (object-array (-> buffer count inc)) current))
  ([{:keys [buffer cursor] :as view} display current]
     (let [string (str buffer \space)]
       (if current
         (aset display cursor (canvas/->Color "white" "black")))
       (areduce display i text ""
                (let [char (nth string i)]
                  (str text (if-let [color (aget display i)]
                              (canvas/html char color)
                              char)))))))

(defn render [{:keys [buffers view]}]
  (let [{:keys [minibuffer]} buffers
        {:keys [y height]} view 
        current (-> buffers window/current buffer/view)
        display (object-array (-> current buffer/show count inc))]
    (str (paint (buffer/view minibuffer) false)
         \newline
         (let [s (paint current display true)
               s (subs s (->> s (re-find (re-pattern (str "([^\n]*\n){" y "}"))) first count))]
           (subs s 0 (->> s (re-find (re-pattern (str "([^\n]*\n){" height "}"))) first count))))))

(def editor
  (Editor.
    (window/open {}
      :*scratch* buffer/empty
      :minibuffer buffer/empty)
    (view/->View 0 0 80 24)
    normal
    "./"))
