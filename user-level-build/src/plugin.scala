package tryp

import sbt._
import Keys._

import TekKeys._

object TekUserLevelBuild
extends AutoPlugin
with Tryplug
{
  override def requires = PluginVersionUpdate

  object autoImport
  {
    def debugDeps = userLevelDebugDeps
  }

  def userLevelDebugDeps = {
    Project(tekUserLevelName, file("."))
      .settings(pluginVersionDefaults: _*)
  }

  def tekUserLevelName = "tek-user-level"

  override def pluginVersionDefaults = super.pluginVersionDefaults ++ List(
    propVersion(sbtAmmoniteVersion, "ammonite", "0.1.2"),
    propVersion(coursierVersion, "coursier", "1.0.3"),
    propVersion(ensimeVersion, "ensime", "2.5.1"),
  )

  import VersionUpdateKeys._

  override def projectSettings =
    super.projectSettings ++
      pluginVersionDefaults ++
      libSettings("tekUserLevel") ++
      Seq(
        TrypKeys.libs := TekLibs,
        bintrayTekResolver,
        autoUpdateVersions := true,
        updateAllPlugins := true,
        updatePluginsExclude += "sbt-coursier",
        versionDirMap ++= {
          val d = projectDir.value
          val dirs = List(d, d / "project")
          Map("tekVersion" -> dirs)
        }
      )

  object TekLibs
  extends Libs
  {
    val tekUserLevelPlugins = plugins(
      plugin("io.get-coursier", "sbt-coursier", coursierVersion, MavenSource),
      plugin("io.tryp", "tek-user-level", TekKeys.tekVersion, BintraySource("tek", "sbt-plugins")),
      plugin("org.ensime", "sbt-ensime", ensimeVersion, MavenSource),
      plugin("net.virtual-void", "sbt-dependency-graph", depGraphVersion, MavenSource),
      // plugin("com.github.alexarchambault", "sbt-ammonite", sbtAmmoniteVersion, MavenSource),
    )
  }
}
