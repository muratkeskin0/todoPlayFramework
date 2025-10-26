name := """todo"""
organization := "internship_school"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.17"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "7.0.2" % Test

// PostgreSQL dependencies
libraryDependencies += "org.postgresql" % "postgresql" % "42.7.3"
libraryDependencies += "com.typesafe.play" %% "play-slick" % "5.3.1"
libraryDependencies += "com.typesafe.play" %% "play-slick-evolutions" % "5.3.1"

// Password hashing
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

// Email dependencies
libraryDependencies += "com.typesafe.play" %% "play-mailer" % "8.0.1"
libraryDependencies += "com.typesafe.play" %% "play-mailer-guice" % "8.0.1"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "internship_school.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "internship_school.binders._"
