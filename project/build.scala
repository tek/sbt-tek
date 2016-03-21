package tryp

import sbt._
import sbt.Keys._

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
    .aggregate(core)
    .settings(
      handlePrefixMap := handlePrefixMap.value ++ List(
        baseDirectory.value -> "tryp.TekBuildKeys."
        )
      )

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      "root" → core,
      "core" → core
    )

    val scalariform = plugin("org.scalariform", "sbt-scalariform",
      scalariformVersion, "daniel-trinh/sbt-scalariform").no

    val release = plugin("com.github.gseitz", "sbt-release", sbtReleaseVersion,
      "sbt/sbt-release").no.bintray("sbt", "sbt-plugin-releases")

    val core = ids(tryplug, scalariform, release)
  }

  override def deps = TekDeps
}
