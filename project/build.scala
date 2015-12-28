package tryp

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtScalariform.autoImport._

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

  lazy val core = pluginSubProject("core")
    .settings(
      scalariformFormat in Compile := Nil,
      scalariformFormat in Test := Nil,
      name := "tek-core"
    )

  lazy val root = pluginProject("root")
    .aggregate(core)

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      "core" → core
    )

    val tryplug = plugin("tryp.sbt", "tryplug", tryplugVersion,
      "tek/tryplug", List("tryplug", "macros")).bintray("tek", "sbt-plugins")

    val scalariform = plugin("org.scalariform", "sbt-scalariform",
      scalariformVersion, "daniel-trinh/sbt-scalariform").no

    val release = plugin("com.github.gseitz", "sbt-release", sbtReleaseVersion,
      "sbt/sbt-release").no.bintray("sbt", "sbt-plugin-releases")

    val core = ids(tryplug, scalariform, release,
      "io.argonaut" %% "argonaut" % "+")

    val root = ids(tryplug)
  }

  override def deps = TekDeps
}
