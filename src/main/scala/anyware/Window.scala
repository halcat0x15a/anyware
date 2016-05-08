package anyware

import scala.xml.Node

case class Window(name: String, x: Int, y: Int, width: Int, height: Int) {

  def toHTML(buffers: Map[String, Buffer]): Node =
    <pre><code>{buffers(name).toHTML.slice(y, y + height).padTo(height, <br />)}</code></pre>

}

object Window {

  def create(name: String): Window = Window(name, 0, 0, 0, 0)

}
