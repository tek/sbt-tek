package tryp

import sbt._
import Keys._

import com.typesafe.sbt.SbtScalariform.autoImport._

import sbtrelease.ReleasePlugin.autoImport._
import ReleaseTransformations._

object TekKeys
{
  import TrypKeys.Tryp
  val tekVersion = settingKey[String]("sbt-tek version") in Tryp
  val ensimeVersion = settingKey[String]("ensime version") in Tryp
  val scalariformVersion = settingKey[String]("scalariform version") in Tryp
  val depGraphVersion = settingKey[String]("dependency-graph version") in Tryp
  val sbtReleaseVersion = settingKey[String]("release version") in Tryp
}
import TekKeys._

object Tek
extends AutoPlugin
with Tryplug
{
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  val autoImport = TekKeys

  override def projectSettings =
    super.projectSettings ++ Seq(
      scalariformFormat in Compile := Nil,
      scalariformFormat in Test := Nil,
      releaseProc,
      releaseIgnoreUntrackedFiles := true,
      resolvers ++= pulsarResolvers,
      publishTo := publishTo.value orElse {
        val repo = if (isSnapshot.value) "snapshots" else "releases"
        Some(repo at s"$pulsarUri/$repo")
      }
    )

  def releaseProc = {
    releaseProcess := Seq[ReleaseStep](
      inquireVersions,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      publishArtifacts,
      tagRelease,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  }

  def nexusPulsar = "nexus.ternarypulsar.com"

  def pulsarUri = nexusUri(nexusPulsar)

  lazy val pulsarResolvers = List("snapshots", "releases").flatMap { tpe =>
    List(true, false) map { maven =>
      val desc = s"${if (maven) "" else "no "}maven"
      Resolver.url(s"pulsar $tpe ($desc)", url(s"$pulsarUri/$tpe"))(
        Patterns(maven, nexusPattern))
    }
  }
}

object TekUserLevel
extends AutoPlugin
with Tryplug
{
  override def requires = UserLevel

  val autoImport = TekKeys

  def tekUserLevelName = "tek-user-level"

  override def projectSettings =
    super.projectSettings ++ deps(tekUserLevelName) ++
    deps.pluginVersions(tekUserLevelName) ++ Seq(
      VersionUpdateKeys.autoUpdateVersions := true,
      VersionUpdateKeys.updateAllPlugins := true
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
      plugin(trypOrg, "tek-core", TekKeys.tekVersion, "tek-core",
        List("tek/sbt-tek", "core")).bintray("tek"),
      plugin("org.ensime", "sbt-ensime", ensimeVersion,
        "ensime/ensime-sbt").maven,
      plugin("org.scalariform", "sbt-scalariform", scalariformVersion,
        "daniel-trinh/sbt-scalariform").maven,
      plugin(vv, dg, depGraphVersion, s"jrudolph/$dg").maven,
      plugin("com.github.gseitz", "sbt-release", sbtReleaseVersion,
        "sbt/sbt-release").bintray("sbt", "sbt-plugin-releases")
    )
  }

  override def deps: Deps = TekDeps
}
