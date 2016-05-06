package anyware

import java.nio.file.{Files, Paths}

import scala.xml.Node

case class Editor(current: String, buffers: Map[String, Buffer], mode: Mode, commands: Map[String, (Editor, List[String]) => Editor]) {

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

  def insert(field: Buffer.Field, value: String): Editor = {
    val buffer = buffers(current).insert(field, value)
    copy(buffers = buffers.updated(current, buffer))
  }

  def move(field: Buffer.Field, n: Int): Editor =
    buffers(current).move(field, n).fold(this) { buffer => 
      copy(buffers = buffers.updated(current, buffer))
    }

  def delete(field: Buffer.Field, n: Int): Editor =
    buffers(current).delete(field, n).fold(this) { buffer => 
      copy(buffers = buffers.updated(current, buffer))
    }

  def moveLine(field: Buffer.Field): Editor =
    buffers(current).moveLine(field).fold(this) { buffer => 
      copy(buffers = buffers.updated(current, buffer))
    }

  def toHTML: Node =
    <html><head><style>{".cursor { color: white; background-color: black; }"}</style></head><body><div>{buffers(current).toHTML}{buffers("*minibuffer*").toHTML}</div></body></html>

  def open(name: String): Editor = {
    val path = Paths.get(name)
    if (Files.exists(path))
      copy(current = name, buffers = buffers + (name -> Buffer.fromString(new String(Files.readAllBytes(path)))))
    else
      this
  }

}

object Editor {

  val commands: Map[String, (Editor, List[String]) => Editor] =
    Map(
      "open" -> { (editor, args) =>
        args match {
          case path :: Nil => editor.open(path)
          case _ => editor
        }
      }
    )

  val default: Editor = Editor("*scratch*", Map("*scratch*" -> Buffer.empty, "*minibuffer*" -> Buffer.empty), Mode.normal, commands)

  def moveLeft(editor: Editor): Editor = editor.move(Buffer.Left, 1)

  def moveRight(editor: Editor): Editor = editor.move(Buffer.Right, 1)

  def moveUp(editor: Editor): Editor = editor.moveLine(Buffer.Left)

  def moveDown(editor: Editor): Editor = editor.moveLine(Buffer.Right)

  def insert(value: String)(editor: Editor): Editor = editor.insert(Buffer.Left, value)

  def delete(editor: Editor): Editor = editor.delete(Buffer.Right, 1)

  def backspace(editor: Editor): Editor = editor.delete(Buffer.Left, 1)

  def setMode(mode: Mode)(editor: Editor): Editor = editor.copy(mode = mode)

  def setCurrent(current: String)(editor: Editor): Editor = editor.copy(current = current)

  def eval(editor: Editor): Editor = editor.eval("*minibuffer*")

}
