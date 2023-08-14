import sbt._
import sbt.Keys._

object BuildSettings {

  def isDotty(version: String) =
    CrossVersion.partialVersion(version).exists(_._1 == 3)

  val scalacParameters = Seq(
    "-deprecation",
    "-feature",
    "-unchecked",
    "-encoding",
    "utf8",
    "-language:implicitConversions",
  )

  def crossScalacOptions(version: String) = {
    if (isDotty(version)) {
      scalacParameters ++ Seq(
        "-Ykind-projector"
      )
    } else {
      scalacParameters ++ Seq(
        "-Ytasty-reader",
        "-Xfatal-warnings"
      )
    }
  }

  def Scala3Project(name: String, dir: String): Project =
    Project(name, file(dir))
      .settings(scalaVersion := Dependencies.Scala3)
      .settings(scalacOptions ++= crossScalacOptions(Dependencies.Scala3))
      .settings(libraryDependencies += Dependencies.tink)
}

