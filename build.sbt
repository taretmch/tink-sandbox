import BuildSettings._
import Dependencies._

ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.github.taretmch"
ThisBuild / organizationName := "taretmch"

lazy val root = Scala3Project("root", ".")

Global / onChangedBuildSource := IgnoreSourceChanges
