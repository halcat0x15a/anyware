package anyware

import java.nio.file.{Files, Paths}

import scala.xml.Node

case class Editor(focus: Boolean, windows: Map[Boolean, Window], buffers: Map[String, Buffer], mode: Mode, commands: Map[String, (Editor, List[String]) => Editor], row: Int, fontSize: Int) {

  def eval(name: String): Editor = {
    val text = buffers(name).toString
    text.split("\\s+", -1).toList match {
      case f :: args if commands.contains(f) =>
        val command = commands(f)
        command(this, args)
      case _ => this
    }
  }

  def run(input: Input): Editor =
    mode.keymap.get(input).fold(mode.default(this, input))(f => f(this))

  def resize(row: Int): Editor =
    copy(windows = Map(true -> windows(true).copy(height = row - 1), false -> windows(false).copy(height = 1)), row = row)

  def updateBuffer(f: Buffer => Buffer): Editor = {
    val window = windows(focus)
    val newBuffer = f(buffers(window.name))
    val y = if (window.y + window.height <= newBuffer.row)
      newBuffer.row - window.height + 1
    else if (window.y > newBuffer.row)
      newBuffer.row
    else
      window.y
    val newWindow = window.copy(y = y)
    copy(windows = windows.updated(focus, newWindow), buffers = buffers.updated(window.name, newBuffer))
  }

  def insert(field: Buffer.Field, value: String): Editor = updateBuffer(buffer => buffer.insert(field, value))

  def delete(field: Buffer.Field, n: Int): Editor = updateBuffer(buffer => buffer.delete(field, n).getOrElse(buffer))

  def move(field: Buffer.Field, n: Int): Editor = updateBuffer(buffer => buffer.move(field, n).getOrElse(buffer))

  def moveLine(field: Buffer.Field): Editor = updateBuffer(buffer => buffer.moveLine(field).getOrElse(buffer))

  def toHTML: Node =
    <html><head><style>{s"body { font-size: ${fontSize}px; line-height: ${fontSize}px; margin: 0; } pre { display: inline-block; margin: 0; } .cursor { color: white; background-color: black; }"}</style></head><body><div>{windows(true).toHTML(buffers)}<br />{windows(false).toHTML(buffers)}</div></body></html>

  def open(name: String): Editor = {
    val path = Paths.get(name)
    if (Files.exists(path))
      copy(focus = true, windows = windows.updated(true, windows(true).copy(name = name)), buffers = buffers + (name -> Buffer.fromString(new String(Files.readAllBytes(path)))))
    else
      this
  }

}

object Editor {

  val commands: Map[String, (Editor, List[String]) => Editor] =
    Map(
      "open" -> open
    )

  val default: Editor = Editor(true, Map(true -> Window.create("*scratch*"), false -> Window.create("*minibuffer*")), Map("*scratch*" -> Buffer.empty, "*minibuffer*" -> Buffer.empty), Mode.normal, commands, 0, 16)

  def moveLeft(editor: Editor): Editor = editor.move(Buffer.Left, 1)

  def moveRight(editor: Editor): Editor = editor.move(Buffer.Right, 1)

  def moveUp(editor: Editor): Editor = editor.moveLine(Buffer.Left)

  def moveDown(editor: Editor): Editor = editor.moveLine(Buffer.Right)

  def insert(value: String)(editor: Editor): Editor = editor.insert(Buffer.Left, value)

  def delete(editor: Editor): Editor = editor.delete(Buffer.Right, 1)

  def backspace(editor: Editor): Editor = editor.delete(Buffer.Left, 1)

  def setMode(mode: Mode)(editor: Editor): Editor = editor.copy(mode = mode)

  def setFocus(focus: Boolean)(editor: Editor): Editor = editor.copy(focus = focus)

  def eval(editor: Editor): Editor = editor.eval("*minibuffer*")

  def open(editor: Editor, args: List[String]): Editor =
    args match {
      case path :: Nil => editor.open(path)
      case _ => editor
    }

}
