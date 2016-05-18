package anyware

case class Mode(keymap: Map[Input, Editor => Editor], default: (Editor, Input) => Editor)

object Mode {

  val normal: Mode =
    Mode(
      Map(
        Input.Text("i") -> Editor.setMode(insert),
        Input.Text("I") -> (Editor.beginningOfLine _ andThen Editor.setMode(insert)),
        Input.Text("A") -> (Editor.endOfLine _ andThen Editor.setMode(insert)),
        Input.Text("a") -> Editor.setMode(insert),
        Input.Text("h") -> Editor.forwardChar,
        Input.Text("j") -> Editor.nextLine,
        Input.Text("k") -> Editor.previousLine,
        Input.Text("l") -> Editor.backwardChar,
        Input.Text("^") -> Editor.beginningOfLine,
        Input.Text("$") -> Editor.endOfLine,
        Input.Text(":") -> Editor.minibuffer,
        Input.Left -> Editor.forwardChar,
        Input.Right -> Editor.backwardChar,
        Input.Up -> Editor.previousLine,
        Input.Down -> Editor.nextLine
      ),
      (editor, _) => editor
    )

  val insert: Mode =
    Mode(
      Map(
        Input.Enter -> Editor.insert("\n"),
        Input.Backspace -> Editor.backspace,
        Input.Left -> Editor.forwardChar,
        Input.Right -> Editor.backwardChar,
        Input.Up -> Editor.previousLine,
        Input.Down -> Editor.nextLine,
        Input.Escape -> Editor.escape
      ),
      (editor, input) => input match {
        case Input.Text(value) => Editor.insert(value)(editor)
        case _ => editor
      }
    )

  val minibuffer: Mode =
    insert.copy(keymap = insert.keymap + (
      Input.Enter -> Editor.eval,
      Input.Tab -> Editor.complete
    ))

}
