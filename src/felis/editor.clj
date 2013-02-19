(ns felis.editor)

(defprotocol Editor
  (keymap [editor])
  (input [editor char]))

(defprotocol KeyCode
  (code [this event]))
