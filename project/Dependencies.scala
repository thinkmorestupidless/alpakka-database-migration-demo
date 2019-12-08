import sbt._

object Versions {

  val logbackClassicVersion       = "1.2.3"
  val scalatestVersion            = "3.0.4"
  val jfakerVersion               = "1.0.1"
}


object Dependencies {
  import Versions._

  val logbackClassic              = "ch.qos.logback"            % "logback-classic"     % logbackClassicVersion
  val jfaker                      = "com.github.javafaker"      % "javafaker"           % jfakerVersion
}
