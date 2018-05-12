package example.views
import example.AudioPlayer
import example.model.BibleFile
import example.utils.Database
import org.scalajs.dom
import org.scalajs.dom.html.{Audio, Div}
import org.scalajs.dom.raw.{Event, HTMLElement, MouseEvent}

import scala.scalajs.js
import scalatags.JsDom.all.{span, _}
import org.scalajs.jquery.{JQuery, jQuery}

import scala.scalajs.js.{Dynamic, UndefOr}
import scala.scalajs.js.annotation.ScalaJSDefined

@js.native
object Amplitude extends js.Any {
  def init(a:js.Any):Unit = js.native
  def playNow(a:js.Any):Unit = js.native
  def audio():Audio = js.native
  def play():Unit = js.native
  def pause():Unit = js.native
  def bindNewElements():Unit = js.native
  def getActiveIndex():Int = js.native
  def setSongPlayedPercentage(p:Double):Unit = js.native
}


@ScalaJSDefined
trait AmplitudeCallbacks extends js.Object {
  /** Occurs before the play method is called */
  var before_play:UndefOr[js.Function0[Any]] = js.undefined

  /** Occurs after the play method is called */
  var after_play:UndefOr[js.Function0[Any]] = js.undefined

  /** Occurs before the stop method is called */
  var before_stop:UndefOr[js.Function0[Any]] = js.undefined

  /** Occurs after the stop method is called */
  var after_stop:UndefOr[js.Function0[Any]] = js.undefined

  /** Occurs when the time has updated */
  var time_update:UndefOr[js.Function0[Any]] = js.undefined

  /** Occurs when an album changes */
  var album_change:UndefOr[js.Function0[Any]] = js.undefined

  /** Occurs when a song has been changed */
  var song_change:UndefOr[js.Function0[Any]] = js.undefined

  var after_pause:UndefOr[js.Function0[Any]] = js.undefined

}

object AudioPlayerView {
    //TODO move it out
  implicit class HtmlOps(val a:HTMLElement) {
    def clickAndTouch(): Boolean = {
      a.click()
      val e = js.Dynamic.newInstance(js.Dynamic.global.Event)("touchend").asInstanceOf[Event]
      a.dispatchEvent(e)
    }
  }

  val obj: Dynamic.literal.type = js.Dynamic.literal
  def play(): Unit = {
    println("AudioPlayerView.play()")
    jQuery(".amplitude-play")(0).clickAndTouch()
  }
  def pause(): Unit = {
    println("AudioPlayerView.pause()")
    jQuery(".amplitude-pause")(0).clickAndTouch()
  }

  def sjsFunction[T](f:js.Function0[T]): js.Function0[T] = f

  def getCurrentSongFile: Option[BibleFile] = {
    val url = songs(Amplitude.getActiveIndex()).url.asInstanceOf[String]
    songsOrginal.find(url == _.url)
  }


  def refresh(): Int = dom.window.setTimeout(() => {
    getCurrentSongFile.foreach(AudioPlayer.onSongChange(_, !Amplitude.audio().paused))
  }, 400)

  val callbacks: AmplitudeCallbacks = new AmplitudeCallbacks {
    song_change = sjsFunction { () => refresh() }
    after_pause = sjsFunction{ () => refresh() }
    after_play = sjsFunction{ () => refresh() }
    after_stop = sjsFunction( () => refresh() )

    time_update = sjsFunction(() =>
      getCurrentSongFile.foreach { s =>
        dom.console.warn("time_update")
        Database.position.set(s, Amplitude.audio().currentTime)
      }
    )
  }

  var songsOrginal: Seq[BibleFile] = Seq.empty
  var songs: js.Array[js.Dynamic] = js.Array()
  var songVersions: js.Dictionary[js.Array[Int]] = js.Dictionary()

  def setPlaylist(list:Seq[BibleFile]): Unit = {

    import scala.scalajs.js.JSConverters._

    songVersions =
      list.zipWithIndex
        .groupBy(_._1.version)
        .map(s => s._1 -> s._2.map(_._2).toJSArray)
        .toJSDictionary

    songsOrginal = list

    songs = list.map { book =>
      obj(
        name = book.book,
        artist = book.versionPartName,
        url = book.url
      ).asInstanceOf[js.Dynamic]
    }(collection.breakOut)

    refreshLastPlaylist()
  }

  def refreshLastPlaylist() = {
    Amplitude.init(obj(
      callbacks = callbacks,
      songs = songs,
      playlists = songVersions
    ))
  }

  def play(book:BibleFile, position:Double = 0, autoplay:Boolean = true): Unit = {
    val idx = songs.indexWhere(_.url == book.url)
    setSongItemPlay.setAttribute("amplitude-song-index", idx.toString)
    setSongItemPlay.setAttribute("amplitude-playlist", book.version)
    setSongItemPlay.clickAndTouch()
    val audio1 = Amplitude.audio()
    audio1.addEventListener("loadeddata", (e:Event) => {
      audio1.currentTime = position
    })
    if(! autoplay) jQuery(".amplitude-pause")(0).clickAndTouch()

  }

  private val progress = typedTag[HTMLElement]("progress")
  private val amplitude = new DataAttribute(List("amplitude"))
  private val setSongItemPlay = span(cls := "amplitude-play", height:="0.1").render

  private val progressView = progress(
    cls := "amplitude-song-played-progress",
    amplitude.main.song.played.progress := "true",
    id := "song-played-progress"
  ).render

  def icon(name:String, icon:String) = div( cls:=("amplitude-button amplitude-" + name), i(cls:=("fa fa-" + icon)))
  val logoHover: Div = div(cls:="hover", img(src:="assets/images/logo_01.png")).render

  val view: Div = div( id:="single-song-player",
    div( cls:="bottom-container",
      div( cls:="control-container",
        icon("prev", "backward"),
        icon("stop", "stop"),
        div( cls:="amplitude-play-pause", amplitude.main.play.pause:="true", id:="play-pause"),
        icon("next", "forward"),

        //todo this items are udes only from code. abstract over them?
        setSongItemPlay,
        div( cls:="amplitude-pause", amplitude.main.play.pause:="true", display.none),
        div( cls:="amplitude-play", amplitude.main.play.pause:="true", display.none),

        div( cls:="meta-container",
          span( amplitude.song.info:="name", amplitude.main.song.info:="true", cls:="song-name"),
          span( amplitude.song.info:="artist", amplitude.main.song.info:="true")
        )
      ),

      div( cls:="time-container",
        span( cls:="current-time",
          span( cls:="amplitude-current-minutes", amplitude.main.current.minutes:="true"),":",span( cls:="amplitude-current-seconds", amplitude.main.current.seconds:="true")
        ),
        span( cls:="duration",
          span( cls:="amplitude-duration-minutes", amplitude.main.duration.minutes:="true"),":",span( cls:="amplitude-duration-seconds", amplitude.main.duration.seconds:="true")
        )
      ),
      progressView,
      logoHover
    )
  ).render

  progressView.addEventListener("click", (e:MouseEvent) => {
    val offset = progressView.getBoundingClientRect()
    val x = e.pageX - offset.left
    Amplitude.setSongPlayedPercentage(x / progressView.offsetWidth * 100 )
  })
}
