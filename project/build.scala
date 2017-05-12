package tryp

import sbt._
import sbt.Keys._

import sbtrelease.ReleasePlugin.autoImport.{releaseVersionBump, releaseIgnoreUntrackedFiles}
import sbtrelease.Version.Bump

import TrypKeys._
import VersionUpdateKeys._

object TekBuildKeys
{
  val ensimeVersion = settingKey[String]("ensime version")
  val scalariformVersion = settingKey[String]("scalariform version")
  val sbtReleaseVersion = settingKey[String]("release version")
  val coursierVersion = settingKey[String]("coursier version")
  val tryplugVersion = TrypKeys.tryplugVersion
}
import TekBuildKeys._

object TekBuild
extends sbt.Build
with Tryplug
{
  override def settings = super.settings ++ pluginVersionDefaults

  def tryplugVersion = TrypKeys.tryplugVersion

  lazy val core = pluginSubProject("core")
    .settings(
      name := "tek-core"
    )

  lazy val userlevelBuild = pluginSubProject("user-level-build")
    .settings(name := "tek-user-level-build")
    .dependsOn(core)

  lazy val userlevel = pluginSubProject("user-level")
    .settings(
      useCoursier := true,
      name := "tek-user-level"
    )
    .dependsOn(core)

  lazy val root = pluginProject("root")
    .settings(
      releaseVersionBump := Bump.Major,
      releaseIgnoreUntrackedFiles := true,
      useCoursier := false,
      handlePrefixMap := Map(
        baseDirectory.value -> "tryp.TekBuildKeys."
      )
    )
    .aggregate(core, userlevel, userlevelBuild)

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      "root" -> userlevel,
      "core" -> core,
      "user-level" -> userlevel
    )

    val ensime = plugin("org.ensime", "sbt-ensime", ensimeVersion, "ensime/ensime-sbt").maven

    val scalariform =
      plugin("org.scalariform", "sbt-scalariform", scalariformVersion, "daniel-trinh/sbt-scalariform").no

    val release = plugin("com.github.gseitz", "sbt-release", sbtReleaseVersion, "sbt/sbt-release")
      .no
      .bintray("sbt", "sbt-plugin-releases")

    val core = ids(tryplug)

    val userlevel = ids(ensime, scalariform, release, coursier)
  }

  override def deps = TekDeps
}
