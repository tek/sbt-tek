package tryp

import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin.autoImport.releaseVersionBump
import sbtrelease.Version.Bump

import TrypKeys._
import VersionUpdateKeys._

object TekBuildKeys
{
  val scalariformVersion = settingKey[String]("scalariform version")
  val sbtReleaseVersion = settingKey[String]("release version")
}
import TekBuildKeys._

object TekBuild
extends sbt.Build
with Tryplug
{
  override def settings = super.settings ++ pluginVersionDefaults

  def tryplugVersion = TrypKeys.tryplugVersion

  def sbtReleaseVersion = TekBuildKeys.sbtReleaseVersion

  lazy val core = pluginSubProject("core")
    .settings(
      name := "tek-core"
    )

  lazy val root = pluginProject("root")
    .settings(releaseVersionBump := Bump.Major)
    .aggregate(core)

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      "root" -> core,
      "core" -> core
    )

    val scalariform = plugin("org.scalariform", "sbt-scalariform",
      scalariformVersion, "daniel-trinh/sbt-scalariform").no

    val release = plugin("com.github.gseitz", "sbt-release", sbtReleaseVersion,
      "sbt/sbt-release").no.bintray("sbt", "sbt-plugin-releases")

    val core = ids(tryplug, scalariform, release, coursier.copy(cond = None))
  }

  override def deps = TekDeps
}
