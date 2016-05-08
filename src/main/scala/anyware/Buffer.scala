package anyware

import scala.xml.{NodeSeq, Text}

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

  def toHTML: NodeSeq = {
    def stringToNode(string: String, nodes: NodeSeq, cursor: Int, i: Int): NodeSeq =
      if (string.length == cursor + i)
        nodes :+ <span>{string.substring(cursor, cursor + i)}<br /></span>
      else if (string.charAt(cursor + i) == '\n')
        stringToNode(string, nodes :+ <span>{string.substring(cursor, cursor + i)}<br /></span>, cursor + i + 1, 0)
      else
        stringToNode(string, nodes, cursor, i + 1)
    val (topNode :+ <span>{leftNode}<br /></span>) = stringToNode(left, NodeSeq.Empty, 0, 0)
    val restNode = if (right.isEmpty) {
      <span>{leftNode}<span class="cursor"> </span><br /></span>
    } else {
      val (<span>{rightNode@_*}</span> +: bottomNode) = stringToNode(right.substring(1), NodeSeq.Empty, 0, 0)
      val centerNode = right.charAt(0) match {
        case '\n' => Seq(<span>{leftNode}<span class="cursor"> </span><br /></span>, <span>{rightNode}</span>)
        case cursor => <span>{leftNode}<span class="cursor">{cursor.toString}</span>{rightNode}</span>
      }
      centerNode ++ bottomNode
    }
    topNode ++ restNode
  }

  override lazy val toString: String = left + right

  lazy val row: Int = {
    def count(text: String, i: Int, n: Int): Int =
      if (text.length == i)
        n
      else if (text.charAt(i) == '\n')
        count(text, i + 1, n + 1)
      else
        count(text, i + 1, n)
    count(left, 0, 0)
  }

  lazy val column: Int = {
    val i = left.lastIndexOf('\n')
    if (i < 0)
      left.length
    else
      left.length - i
  }

}

object Buffer {

  sealed abstract class Field

  case object Left extends Field

  case object Right extends Field

  val empty: Buffer = new Buffer("", "")

  def fromString(value: String): Buffer = new Buffer("", value)

}
