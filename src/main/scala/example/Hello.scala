package example

import example.model.{Bible, BibleFile}
import example.views.BibleViews
import org.scalajs.dom
import org.scalajs.dom.html.{Div, Heading}
import org.scalajs.jquery.jQuery
import scalatags.JsDom.all._

object Hello {

  def main(args: Array[String]): Unit = {
    dom.document.body.innerHTML = ""
    dom.document.body.appendChild(view)
    jQuery
    dom.window.setTimeout(() => {
      header.style.color = "pink"
    }, 2200)
  }

  val header: Heading = h1("hello world").render

  val view: Div = div(
    header,
    Bible.nt.books.map(BibleViews.book),
    footer
  ).render
}
