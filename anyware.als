one sig Editor {
  name: String,
  history: History,
  buffers: set (String - name) one -> one (History - history)
}

sig History {
  buffer: Buffer
}

sig Buffer {
  lefts: String,
  rights: String
}

pred show (e: Editor) {
  "foo" in String
  "bar" in String
  "baz" in String
}

run show
