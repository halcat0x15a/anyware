package anyware

import scala.xml.Node

case class Buffer(left: String, right: String) {

  def get(field: Buffer.Field): String =
    field match {
      case Buffer.Left => left
      case Buffer.Right => right
    }

  def insert(field: Buffer.Field, value: String): Buffer =
    field match {
      case Buffer.Left => copy(left = left + value)
      case Buffer.Right => copy(right = value + right)
    }

  def delete(field: Buffer.Field, n: Int): Option[Buffer] =
    field match {
      case Buffer.Left if n <= left.length => Some(copy(left = left.substring(0, left.length - n)))
      case Buffer.Right if n <= right.length => Some(copy(right = right.substring(n)))
      case _ => None
    }

  def move(field: Buffer.Field, n: Int): Option[Buffer] =
    for (buffer <- delete(field, n)) yield {
      val reverse = field match {
        case Buffer.Left => Buffer.Right
        case Buffer.Right => Buffer.Left
      }
      val value = field match {
        case Buffer.Left => left.substring(left.length - n)
        case Buffer.Right => right.substring(0, n)
      }
      buffer.insert(reverse, value)
    }

  def moveLine(field: Buffer.Field): Option[Buffer] = {
    val n = field match {
      case Buffer.Left =>
        val i = left.lastIndexOf("\n")
        if (i >= 0)
          Some(left.length - i)
        else
          None
      case Buffer.Right =>
        val i = right.indexOf("\n")
        if (i >= 0)
          Some(i + 1)
        else
          None
    }
    n.flatMap(n => move(field, n))
  }

  def toHTML: Node = {
    val rightText = if (right.isEmpty) "" else right.substring(1)
    val cursorText = if (right.isEmpty) " " else {
      val cursor = right.charAt(0)
      if (cursor == '\n') " \n" else cursor.toString
    }
    <pre><code>{left}<span class="cursor">{cursorText}</span>{rightText}</code></pre>
  }

  override def toString: String = left + right

}

object Buffer {

  sealed abstract class Field

  case object Left extends Field

  case object Right extends Field

  val empty: Buffer = new Buffer("", "")

  def fromString(value: String): Buffer = new Buffer("", value)

}
