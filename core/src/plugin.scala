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
      scalariformFormat in Test := Nil
    )
}

object TekUserLevel
extends AutoPlugin
with Tryplug
{
  override def requires = UserLevel

  val autoImport = TekKeys

  def tekUserLevelName = "tek-user-level"

  def updateTekVersion = Def.task {
    implicit val log = streams.value.log
    val updater = new Versions {
      def projectDir = Some(baseDirectory.value / "project")
    }
    updater.update(pspec("tek", "sbt-plugins", "tek-core", TekKeys.tekVersion))
  }

  def pulsar = "nexus.ternarypulsar.com"

  override def projectSettings =
    super.projectSettings ++ deps(tekUserLevelName) ++
    deps.pluginVersions(tekUserLevelName) ++ Seq(
      VersionUpdateKeys.autoUpdateVersions := true,
      update <<= update dependsOn updateTekVersion,
      resolvers ++= List("snapshots", "releases").map { tpe ⇒
        s"pulsar $tpe" at s"${nexusUri(pulsar)}/$tpe"
      },
      releaseProc
    )

  def releaseProc = {
    releaseProcess := Seq[ReleaseStep](
      checkSnapshotDependencies,
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

  object TekDeps
  extends PluginDeps
  {
    override def deps = super.deps ++ Map(
      tekUserLevelName → tekUserLevel
    )

    val huy = "com.hanhuy.sbt"
    val sdkName = "android-sdk-plugin"
    val protifyName = "protify"

    val tekUserLevel = ids(
      pd(trypOrg, "tek-core", TekKeys.tekVersion, "tek", "sbt-plugins",
        "tek-core", "tek/sbt-tek", "core"),
      pd("org.ensime", "ensime-sbt", ensimeVersion, "", "",
        "ensime/ensime-sbt"),
      pd("org.scalariform", "sbt-scalariform", scalariformVersion,
        "joprice", "maven", "daniel-trinh/sbt-scalariform"),
      pd("net.virtual-void", "sbt-dependency-graph", depGraphVersion,
        "jrudolph", "maven", "jrudolph/sbt-dependency-graph"),
      pd("com.github.gseitz", "sbt-release", sbtReleaseVersion, "sbt",
        "sbt-plugin-releases", "sbt/sbt-release")
    )
  }

  override def deps: Deps = TekDeps
}
