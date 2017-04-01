package tryp

import sbt._

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
    propVersion(sbtAmmoniteVersion, "ammonite", "0.1.2")
  )

  import VersionUpdateKeys._

  override def projectSettings =
    super.projectSettings ++ deps(tekUserLevelName) ++ pluginVersionDefaults ++
    deps.pluginVersions(tekUserLevelName) ++ Seq(
      TrypKeys.useCoursier := true,
      autoUpdateVersions := true,
      updateAllPlugins := true,
      versionDirMap ++= {
        val d = projectDir.value
        val dirs = List(d, d / "project")
        Map("tekVersion" -> dirs)
      }
    )

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      tekUserLevelName -> tekUserLevel
    )

    val dg = "sbt-dependency-graph"
    val vv = "net.virtual-void"

    val tekUserLevel = ids(
      coursier,
      plugin(trypOrg, "tek-user-level", TekKeys.tekVersion, "tek/sbt-tek", List("user-level")).bintray("tek"),
      plugin("org.ensime", "sbt-ensime", ensimeVersion, "ensime/ensime-sbt").maven,
      plugin("org.scalariform", "sbt-scalariform", scalariformVersion, "daniel-trinh/sbt-scalariform").maven,
      plugin(vv, dg, depGraphVersion, s"jrudolph/$dg").maven,
      plugin("com.github.gseitz", "sbt-release", sbtReleaseVersion, "sbt/sbt-release")
        .bintray("sbt", "sbt-plugin-releases"),
      plugin("com.github.alexarchambault", "sbt-ammonite", sbtAmmoniteVersion, "alexarchambault/sbt-ammonite").maven
    )
  }

  override def deps: Deps = TekDeps
}
