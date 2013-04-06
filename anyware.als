sig Editor {
  name: String,
  history: History,
  buffers: set (String - name) one -> one (History - history)
}

sig History {
  buffer: Buffer,
  pasts, futures: set History - this
}

sig Buffer {
  left: String,
  right: String
}

pred show (e: Editor) {
  "foo" in String
  "bar" in String
  "baz" in String
}

run show
