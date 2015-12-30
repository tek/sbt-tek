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
      android.Keys.updateCheck in android.protify.Keys.Protify := ()
    )

  def releaseProc = {
    releaseProcess := Seq[ReleaseStep](
      inquireVersions,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      publishArtifacts,
      setNextVersion,
      commitNextVersion,
      pushChanges
    )
  }
}

object TekUserLevel
extends AutoPlugin
with Tryplug
{
  override def requires = UserLevel && android.protify.Plugin

  val autoImport = TekKeys

  def tekUserLevelName = "tek-user-level"

  lazy val updateTekVersion =
    projectUpdater("tek", "sbt-plugins", "tek-core", TekKeys.tekVersion)

  def pulsar = "nexus.ternarypulsar.com"

  override def projectSettings =
    super.projectSettings ++ deps(tekUserLevelName) ++
    deps.pluginVersions(tekUserLevelName) ++ Seq(
      VersionUpdateKeys.autoUpdateVersions := true,
      update <<= update dependsOn updateTekVersion,
      resolvers ++= List("snapshots", "releases").map { tpe ⇒
        s"pulsar $tpe" at s"${nexusUri(pulsar)}/$tpe"
      }
    )

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      tekUserLevelName → tekUserLevel
    )

    val dg = "sbt-dependency-graph"
    val vv = "net.virtual-void"

    val tekUserLevel = ids(
      plugin(trypOrg, "tek-core", TekKeys.tekVersion, "tek-core",
        List("tek/sbt-tek", "core")).bintray("tek"),
      plugin("org.ensime", "ensime-sbt", ensimeVersion,
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
