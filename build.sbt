// *****************************************************************************
// Projects
// *****************************************************************************
lazy val `akka-http-rest-client` =
  (project in file("client/akka-http"))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.sprayJson % Test,
        library.scalaTest % Test
      )
    )

lazy val `akka-http-rest-client-sample` =
  (project in file("sample/client"))
    .settings(settings)
    .settings(
      libraryDependencies ++= Seq(
        library.akkaHttp,
        library.circe,
        library.circeGeneric,
        library.circeAkkaHttp,
        library.sprayJson
      ),
      publishArtifact := false
    ).dependsOn(`akka-http-rest-client`)

lazy val root = (project in file("."))
  .settings(settings)
  .settings(
    name := "akka-http-client-project",
    aggregate in update := false,
    publishArtifact := false
  )
  .aggregate(`akka-http-rest-client`, `akka-http-rest-client-sample`)


// *****************************************************************************
// Library dependencies
// *****************************************************************************
lazy val library =
  new {

    object Version {
      val akkaHttp = "10.0.11"
      val scalaTest = "3.0.4"
      val circe = "0.9.3"
      val circeAkkaHttp = "1.21.0"
    }

    val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
    val sprayJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
    val circeAkkaHttp = "de.heikoseeberger" %% "akka-http-circe" % Version.circeAkkaHttp
    val circe = "io.circe" %% "circe-core" % Version.circe
    val circeGeneric = "io.circe" %% "circe-generic" % Version.circe
    val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest
  }

// *****************************************************************************
// Settings
// *****************************************************************************
lazy val settings = projectSettings ++ publishSettings

lazy val projectSettings =
  Seq(
    scalaVersion := "2.12.7",
    organization := "net.softler",
    version := "0.2.1",
    organizationName := "Tobias Frischholz",
    startYear := Some(2017),
    licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
	javacOptions ++= Seq("-source", "1.8"),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-feature",
      "-unchecked",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfatal-warnings",
      "-Yno-adapted-args",
      "-Xfuture"
    ),
    sources in (Compile, doc) := Seq.empty
  )

lazy val publishSettings =
  Seq(
    homepage := Some(url("https://github.com/Freshwood/akka-http-rest-client")),
    scmInfo := Some(ScmInfo(url("https://github.com/Freshwood/akka-http-rest-client"),
      "git@github.com:Freshwood/akka-http-rest-client.git")),
    developers += Developer("freshwood",
      "Tobias Frischholz",
      "tfrischholz@kabelmail.de",
      url("https://github.com/freshwood")),
    pomIncludeRepository := (_ => false),
    bintrayPackage := "akka-http-rest-client"
  )
