import sbt._
import Keys._

object Common {
	val settings: Seq[Setting[_]] = Seq(
		version := "2.2.5",
		organization := "org.jatos",
		scalaVersion := "2.11.8"
	)
}