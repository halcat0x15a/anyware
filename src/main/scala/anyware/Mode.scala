package anyware

case class Mode(keymap: Map[Input, Editor => Editor], default: (Editor, Input) => Editor)

object Mode {

  val normal: Mode =
    Mode(
      Map(
        Input.Text("i") -> Editor.setMode(insert),
        Input.Text("h") -> Editor.moveLeft,
        Input.Text("j") -> Editor.moveDown,
        Input.Text("k") -> Editor.moveUp,
        Input.Text("l") -> Editor.moveRight,
        Input.Text(":") -> (Editor.setCurrent("*minibuffer*") _ andThen Editor.setMode(minibuffer)),
        Input.Left -> Editor.moveLeft,
        Input.Right -> Editor.moveRight,
        Input.Up -> Editor.moveUp,
        Input.Down -> Editor.moveDown
      ),
      (editor, _) => editor
    )

  val insert: Mode =
    Mode(
      Map(
        Input.Enter -> Editor.insert("\n"),
        Input.Backspace -> Editor.backspace,
        Input.Left -> Editor.moveLeft,
        Input.Right -> Editor.moveRight,
        Input.Up -> Editor.moveUp,
        Input.Down -> Editor.moveDown,
        Input.Escape -> Editor.setMode(normal)
      ),
      (editor, input) => input match {
        case Input.Text(value) => Editor.insert(value)(editor)
        case _ => editor
      }
    )

  val minibuffer: Mode =
    insert.copy(keymap = insert.keymap.updated(Input.Enter, Editor.eval))

}
