package tryp

import sbt._

object TekKeys
{
  import TrypKeys.Tryp
  val tekVersion = settingKey[String]("sbt-tek version") in Tryp
  val ensimeVersion = settingKey[String]("ensime version") in Tryp
  val scalariformVersion = settingKey[String]("scalariform version") in Tryp
  val depGraphVersion = settingKey[String]("dependency-graph version") in Tryp
  val sbtReleaseVersion = settingKey[String]("release version") in Tryp
  val sbtAmmoniteVersion = settingKey[String]("ammonite version") in Tryp
  val splain = settingKey[Boolean]("use splain") in Tryp
  val splainBreakInfix = settingKey[Int]("splain infix line break threshold") in Tryp
  val splainTruncRefined = settingKey[Int]("splain infix refinement template length threshold") in Tryp
  val splainVersion = settingKey[String]("splain version") in Tryp
  val trypArtifactRepo = settingKey[Boolean]("add resolvers to tryp nexus") in Tryp
}

trait TekKeys
{
  val tekVersion = TekKeys.tekVersion
  val ensimeVersion = TekKeys.ensimeVersion
  val scalariformVersion = TekKeys.scalariformVersion
  val depGraphVersion = TekKeys.depGraphVersion
  val sbtReleaseVersion = TekKeys.sbtReleaseVersion
  val sbtAmmoniteVersion = TekKeys.sbtAmmoniteVersion
  val splain = TekKeys.splain
  val splainBreakInfix = TekKeys.splainBreakInfix
  val splainTruncRefined = TekKeys.splainTruncRefined
  val splainVersion = TekKeys.splainVersion
  val coursierVersion = TrypKeys.coursierVersion
}

object GlobalKeysPlug
extends AutoPlugin
{
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport
  extends TekKeys
}
