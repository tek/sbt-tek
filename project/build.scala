package tryp

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtScalariform.autoImport._

import bintray.BintrayPlugin

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

  lazy val common = Seq(
    publishTo := {
      val base = nexusUri("nexus.ternarypulsar.com")
      val repo = if (isSnapshot.value) "snapshots" else "releases"
      Some(repo at s"$base/$repo")
    },
    publishMavenStyle := true,
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false
  )

  lazy val core = pluginSubProject("core")
    .settings(common: _*)
    .settings(
        resolvers += Resolver.url(
          "bintray-tek-sbt",
          url("https://dl.bintray.com/tek/sbt-plugins")
        )(Resolver.ivyStylePatterns),
      scalariformFormat in Compile := Nil,
      scalariformFormat in Test := Nil,
      name := "tek-core"
    )
    .disablePlugins(BintrayPlugin)

  lazy val root = pluginProject("root")
    .settings(common: _*)
    .disablePlugins(BintrayPlugin)

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      "core" → core
    )

    val tryplug = pd("tryp.sbt", "tryplug", tryplugVersion, "tek",
      "sbt-plugins", "tek/tryplug", "tryplug", "macros")

    val scalariform = pd("org.scalariform", "sbt-scalariform",
      scalariformVersion, "", "", "daniel-trinh/sbt-scalariform").no

    val release = pd("com.github.gseitz", "sbt-release", sbtReleaseVersion,
      "sbt", "sbt-plugin-releases", "sbt/sbt-release")

    val core = ids(tryplug, scalariform, release)

    val root = ids(tryplug)
  }

  override def deps = TekDeps
}
