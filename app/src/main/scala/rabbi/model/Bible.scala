package rabbi.model
import rabbi.BibleMp3Data
import rabbi.player.Song
import rabbi.utils.Implicits._

import scala.util.Try

case class Bible(nt:BibleTestament, ot:BibleTestament) {
  def all = new BibleTestament("", ot.files ++ nt.files) {
    val ntIndex = ot.files.length
  }

}

object Bible extends Bible(
  nt = BibleTestament("Nowy Testament", BibleMp3Data.NT),
  ot = BibleTestament("Stary Testament", BibleMp3Data.OT)
)

case class BibleTestament(name:String, files:Seq[BibleFile]) {
  val books:Seq[Book] = files.groupByOrdered(_.shortBook).map {
    case (n, f) =>
      val head = f.head
      Book(head.book, head.shortBook, head.bookKind, f.toSeq)
  }.toSeq
}

case class Book(name:String, short:String, group:String , files:Seq[BibleFile]) {
  def getVersion: Map[String, Seq[BibleFile]] = {
    val res = files.groupBy(s => s.version)
    res.withDefault(c => res("cała"))
  }

  def fullVersion = getVersion("cała")
  def partedVersion = getVersion("podzielona")
}

case class BibleFile(
  url:String,
  book:String,
  shortBook:String,
  version:String,
  versionPartName:String,
  time:String,
  bookKind:String
) extends Song {

  def progressOf(time: Double) = Math.max(0, Math.min(1, time / timeReal))

  lazy val timeReal:Double = time.split(":") match {
    case Array(h, m, s) => 3600 * h.toInt + m.toInt * 60 + s.toDouble
    case Array(m, s) =>  (m.toInt * 60) + s.toDouble
    case Array(s) => s.toDouble
    case other => 0.01
  }

  def tryTimeReal = Try(timeReal).getOrElse(0.02)
}

object BibleFile {
  val empty: BibleFile = BibleFile("","","","","","","")
}