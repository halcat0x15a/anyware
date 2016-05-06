lazy val commonSettings = Seq(
  organization := "org.halcat",
  version := "0.1.0",
  scalaVersion := "2.11.8",
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" %% "scala-xml" % "1.0.5",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.4"
  ),
  scalacOptions ++= Seq("-Xexperimental")
)

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "anyware",
    fork in run := true
  )
