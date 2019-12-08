import Dependencies._

version in ThisBuild := "1.0"
scalaVersion in ThisBuild := "2.13.1"

lazy val akkaVersion = "2.5.23"

lazy val `alpakka-jdbc-demo` = (project in file("migration-stream"))
  .enablePlugins(JavaAppPackaging, DockerPlugin, Cinnamon)
  .settings(common)
  .settings(dockerSettings)
  .settings(
    libraryDependencies ++= Seq(
      Cinnamon.library.cinnamonCHMetrics,
      Cinnamon.library.cinnamonAkkaStream,
      Cinnamon.library.cinnamonPrometheus,
      Cinnamon.library.cinnamonPrometheusHttpServer,
      "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "1.1.2",
      "com.lightbend.akka" %% "akka-stream-alpakka-couchbase" % "1.1.2",
      logbackClassic,
      "org.postgresql"  % "postgresql" % "42.1.4",
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    ),
    cinnamon in run := true,
    cinnamon in test := true
  )

lazy val `data-generator` = (project in file("data-generator"))
  .enablePlugins(JavaAppPackaging)
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      "com.lightbend.akka" %% "akka-stream-alpakka-csv" % "1.1.2",
      logbackClassic,
      jfaker
    )
  )

def common = Seq(
  javacOptions in Compile := Seq("-g", "-encoding", "UTF-8", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters", "-Werror")
)

dockerUpdateLatest := true

def dockerSettings = Seq(
  dockerUpdateLatest := true,
  dockerBaseImage := getDockerBaseImage(),
  dockerExposedPorts := Seq(8080, 8558, 2550, 9000, 9001)
)

def getDockerBaseImage(): String = sys.props.get("java.version") match {
  case Some(v) if v.startsWith("11") => "adoptopenjdk/openjdk11"
  case _ => "adoptopenjdk/openjdk8"
}
