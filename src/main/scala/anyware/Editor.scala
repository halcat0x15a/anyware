package anyware

import java.nio.file.{Files, Paths}
import java.util.stream.Collectors

import scala.collection.JavaConverters._
import scala.xml.Node

case class Editor(focus: Boolean, windows: Map[Boolean, Window], buffers: Map[String, Buffer], mode: Mode, commands: Map[String, (Editor, List[String]) => Editor], row: Int, fontSize: Int) {

  def parse(name: String): Option[(String, List[String])] =
    buffers(name).toString.split("\\s+", -1).toList match {
      case f :: args => Some((f, args))
      case _ => None
    }

  def eval: Editor =
    parse("*minibuffer*") match {
      case Some((f, args)) if commands.contains(f) =>
        val command = commands(f)
        command(this, args)
      case _ => this
    }

  def showTable(name: String, values: List[String]): Editor =
    copy(buffers = buffers.updated(name, Buffer.fromString(values.mkString("    "))))

  def complete: Editor =
    parse("*minibuffer*") match {
      case Some((f, Nil)) => showTable("*completion*", commands.keys.filter(_.startsWith(f)).toList)
      case Some((f, filename :: Nil)) =>
        val path = Paths.get(filename)
        if (Files.isDirectory(path)) {
          showTable("*completion*", Files.list(path).collect(Collectors.toList()).asScala.map(p => path.relativize(p).toString).toList)
        } else {
          val parent = path.toAbsolutePath.getParent
          val name = path.getFileName
          val paths = Files.list(parent).collect(Collectors.toList()).asScala.map(path => parent.relativize(path)).filter(_.toString.startsWith(name.toString))
          if (paths.size == 1) {
            val file = paths.head
            val suffix = if (Files.isDirectory(parent.resolve(file))) "/" else ""
            copy(buffers = buffers.updated("*minibuffer*", Buffer(s"$f ${parent.resolve(file)}$suffix", "")))
          } else {
            showTable("*completion*", paths.map(_.toString).toList)
          }
        }
      case _ => showTable("*completion*", commands.keys.toList)
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

  def toHTML: Node =
    <html><head><style>{s"body { font-size: ${fontSize}px; line-height: ${fontSize}px; margin: 0; } pre { display: inline-block; margin: 0; } .cursor { color: white; background-color: black; }"}</style></head><body><div>{windows(true).toHTML(buffers)}<br />{windows(false).toHTML(buffers)}<br />{buffers("*completion*").toHTML}</div></body></html>

  def escape: Editor = copy(focus = true, mode = Mode.normal)

  def edit(name: String): Editor = {
    val path = Paths.get(name)
    if (Files.exists(path)) {
      val bytes = Files.readAllBytes(path)
      copy(windows = windows.updated(true, windows(true).copy(name = name)), buffers = buffers + (name -> Buffer.fromString(new String(bytes, "UTF-8"))))
    } else {
      this
    }
  }

  def write(): Editor = {
    val window = windows(true)
    Files.write(Paths.get(window.name), buffers(window.name).toString.getBytes)
    this
  }

}

object Editor {

  val commands: Map[String, (Editor, List[String]) => Editor] =
    Map(
      "edit" -> edit,
      "write" -> write
    )

  val buffers: Map[String, Buffer] =
    Map(
      "*scratch*" -> Buffer.empty,
      "*minibuffer*" -> Buffer.empty,
      "*completion*" -> Buffer.empty
    )

  val default: Editor = Editor(true, Map(true -> Window.create("*scratch*"), false -> Window.create("*minibuffer*")), buffers, Mode.normal, commands, 0, 16)

  def forwardChar(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.move(Buffer.Left, 1).getOrElse(buffer))

  def backwardChar(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.move(Buffer.Right, 1).getOrElse(buffer))

  def nextLine(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.moveLine(Buffer.Right).flatMap(_.move(Buffer.Right, 1)).getOrElse(buffer))

  def previousLine(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.moveLine(Buffer.Left).flatMap(_.move(Buffer.Left, 1)).getOrElse(buffer))

  def beginningOfLine(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.moveLine(Buffer.Left).getOrElse(buffer))

  def endOfLine(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.moveLine(Buffer.Right).getOrElse(buffer))

  def insert(value: String)(editor: Editor): Editor = editor.updateBuffer(_.insert(Buffer.Left, value))

  def delete(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.delete(Buffer.Right, 1).getOrElse(buffer))

  def backspace(editor: Editor): Editor = editor.updateBuffer(buffer => buffer.delete(Buffer.Left, 1).getOrElse(buffer))

  def setMode(mode: Mode)(editor: Editor): Editor = editor.copy(mode = mode)

  def escape(editor: Editor): Editor = editor.escape

  def eval(editor: Editor): Editor = editor.eval.escape

  def complete(editor: Editor): Editor = editor.complete

  def minibuffer(editor: Editor): Editor = editor.copy(focus = false, buffers = editor.buffers.updated("*minibuffer*", Buffer.empty), mode = Mode.minibuffer)

  def edit(editor: Editor, args: List[String]): Editor =
    args match {
      case path :: Nil => editor.edit(path)
      case _ => editor
    }

  def write(editor: Editor, args: List[String]): Editor =
    args match {
      case Nil => editor.write()
      case _ => editor
    }

}
