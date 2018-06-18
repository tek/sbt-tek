package tryp

import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin.autoImport.{releaseVersionBump, releaseIgnoreUntrackedFiles}
import sbtrelease.Version.Bump

import VersionUpdateKeys._

object TekBuild
extends AutoPlugin
{
  override def trigger = noTrigger

  object autoImport
  {
    val ensimeVersion = settingKey[String]("ensime version")
    val scalariformVersion = settingKey[String]("scalariform version")
    val sbtReleaseVersion = settingKey[String]("release version")
    val coursierVersion = settingKey[String]("coursier version")
    val tryplugVersion = settingKey[String]("tryplug version")
    val TekLibs = tryp.TekLibs
  }
}

object Plugins
extends Libs
{
  import TekBuild.autoImport._
  val ensime = plugin("org.ensime", "sbt-ensime", ensimeVersion, MavenSource)
  def tryplug =
    plugin("io.tryp", "tryplug", tryplugVersion, BintraySource("tek", "sbt-plugins"))
  val coursier = plugin("io.get-coursier", "sbt-coursier", coursierVersion, MavenSource)
}

object TekLibs
extends Libs
{
  import Plugins._
  val corePlugins = plugins(tryplug)
  val rootPlugins = plugins(ensime, coursier)
  val userlevelPlugins = rootPlugins
}
