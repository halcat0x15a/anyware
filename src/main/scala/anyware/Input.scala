package anyware

sealed abstract class Input

object Input {

  case class Text(value: String) extends Input

  case object Left extends Input

  case object Right extends Input

  case object Up extends Input

  case object Down extends Input

  case object Escape extends Input

  case object Enter extends Input

  case object Backspace extends Input

  case object Tab extends Input

}
