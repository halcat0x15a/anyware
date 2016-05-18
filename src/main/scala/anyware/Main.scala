package anyware

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.input.KeyCode
import javafx.scene.web.WebView
import javafx.stage.Stage

class Main extends Application {

  def start(stage: Stage): Unit = {
    var editor = Editor.default
    val view = new WebView
    val engine = view.getEngine
    val scene = new Scene(view)
    scene.setOnKeyPressed { e =>
      val input = e.getCode match {
        case KeyCode.LEFT => Input.Left
        case KeyCode.RIGHT => Input.Right
        case KeyCode.UP => Input.Up
        case KeyCode.DOWN => Input.Down
        case KeyCode.ESCAPE => Input.Escape
        case KeyCode.ENTER => Input.Enter
        case KeyCode.BACK_SPACE => Input.Backspace
        case KeyCode.TAB => Input.Tab
        case _ => Input.Text(e.getText)
      }
      editor = editor.run(input)
      engine.loadContent(editor.toHTML.toString)
    }
    scene.heightProperty.addListener { (_, _, value) =>
      editor = editor.resize(math.floor(value.doubleValue / editor.fontSize).toInt)
    }
    stage.setTitle("Anyware")
    stage.setScene(scene)
    stage.show()
  }

}

object Main extends App {

  Application.launch(classOf[Main])

}
