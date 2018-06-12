package example.views.player

import example.model.Bible
import example.player.AudioPlayer
import example.utils.bootstrap.BTabs
import example.views.InfoView

import scalatags.JsDom.all._

class BibleViews(audioPlayer:AudioPlayer) {
  val colorsST = Seq("#e00b3c", "#9a13dd", "#1357dd", "#13ddae", "#13b5dd")
  val colorsNT = Seq("#ddac25", "#6113dd", "#13b5dd", "#d7dd13")

  val nt = new BibleTestamentView(Bible.nt, colorsNT, audioPlayer)
  val ot = new BibleTestamentView(Bible.ot, colorsST, audioPlayer)
  val info = new InfoView() {
    val icon = Seq(
      padding := "1.18em 0",
      span(cls := "fa fa-info-circle fa-2x")
    )
  }

  val tabs = new BTabs {
    val stTab   = tab("Stary Testament", ot.view)
    val ntTab   = tab("Nowy Testament", nt.view)
    val infoTab = tab(info.icon, div(id := "app-info", cls := "tab-pane", info.view).render)
    //ugly :(
    infoTab.headerView.classList.add("infoTab")

  }

  nt.view.classList.add("active")
}
