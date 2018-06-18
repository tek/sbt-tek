package tryp

import sbt._

object TekKeys
{
  import TrypKeys.Tryp
  val tekVersion = settingKey[String]("sbt-tek version") in Tryp
  val coursierVersion = settingKey[String]("coursier version") in Tryp
  val ensimeVersion = settingKey[String]("ensime version") in Tryp
  val depGraphVersion = settingKey[String]("dependency-graph version") in Tryp
  val sbtAmmoniteVersion = settingKey[String]("ammonite version") in Tryp
  val splain = settingKey[Boolean]("use splain") in Tryp
  val splainBreakInfix = settingKey[Int]("splain infix line break threshold") in Tryp
  val splainTruncRefined = settingKey[Int]("splain infix refinement template length threshold") in Tryp
  val splainVersion = settingKey[String]("splain version") in Tryp
  val trypArtifactRepo = settingKey[Boolean]("add resolvers to tryp nexus") in Tryp
}

trait TekKeys
{
  def tekVersion = TekKeys.tekVersion
  def coursierVersion = TekKeys.coursierVersion
  def ensimeVersion = TekKeys.ensimeVersion
  def depGraphVersion = TekKeys.depGraphVersion
  def sbtAmmoniteVersion = TekKeys.sbtAmmoniteVersion
  def splain = TekKeys.splain
  def splainBreakInfix = TekKeys.splainBreakInfix
  def splainTruncRefined = TekKeys.splainTruncRefined
  def splainVersion = TekKeys.splainVersion
}

object GlobalKeysPlugin
extends AutoPlugin
{
  object autoImport
  extends TekKeys
}
