package tryp

import java.nio.file.Paths

import util.Try

import sbt._
import Keys._
import xsbti.{Position, Maybe}

import org.ensime.EnsimeKeys.ensimeIgnoreMissingDirectories

import com.typesafe.sbt.SbtScalariform.autoImport._

import sbtrelease.ReleasePlugin.autoImport._
import ReleaseTransformations._
import sbtrelease.Version.Bump

import coursier.Keys._

object TekKeys
{
  import TrypKeys.Tryp
  val tekVersion = settingKey[String]("sbt-tek version") in Tryp
  val ensimeVersion = settingKey[String]("ensime version") in Tryp
  val scalariformVersion = settingKey[String]("scalariform version") in Tryp
  val depGraphVersion = settingKey[String]("dependency-graph version") in Tryp
  val sbtReleaseVersion = settingKey[String]("release version") in Tryp
  val sbtAmmoniteVersion = settingKey[String]("ammonite version") in Tryp
}
import TekKeys._

object Tek
extends AutoPlugin
with Tryplug
{
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

  val autoImport = TekKeys

  def majorPlugins = List("tryp", "tryplug", "tek")

  def posMapper(base: String)(pos: Position) = {
    Try {
      val sourcePath =
        if (pos.sourcePath.isDefined) Some(pos.sourcePath.get)
        else None
      sourcePath.flatMap { sp =>
        val relative = Paths.get(base).relativize(Paths.get(sp))
        if (relative.startsWith("..")) None
        else Some(new Position {
            def line = pos.line
            def lineContent = pos.lineContent
            def offset = pos.offset
            def pointer = pos.pointer
            def pointerSpace = pos.pointerSpace
            def sourceFile = pos.sourceFile
            def sourcePath = Maybe.just(relative.toString)
          })
      }
    } getOrElse None
  }

  override def projectSettings =
    super.projectSettings ++ Seq(
      scalariformFormat in Compile := Nil,
      scalariformFormat in Test := Nil,
      releaseProc,
      ensimeIgnoreMissingDirectories := true,
      coursierUseSbtCredentials := true,
      releaseIgnoreUntrackedFiles := true,
      resolvers ++= pulsarResolvers,
      publishTo := publishTo.value orElse {
        val repo = if (isSnapshot.value) "snapshots" else "releases"
        Some(repo at s"$pulsarUri/$repo")
      },
      releaseVersionBump := {
        if (majorPlugins.contains(name.value)) Bump.Major
        else Bump.Next
      },
      sourcePositionMappers += posMapper((baseDirectory in ThisBuild).value.toString) _,
      resolvers += Resolver.bintrayRepo("tek", "maven"),
      addCompilerPlugin("tryp" %% "splain" % "0.1.21"),
      scalacOptions ++= List("-P:splain:bounds", "-P:splain:breakinfix:20")
    )

  def releaseProc = {
    releaseProcess := Seq[ReleaseStep](
      inquireVersions,
      setReleaseVersion,
      commitReleaseVersion,
      publishArtifacts,
      tagRelease,
      setNextVersion,
      commitNextVersion
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

  override def pluginVersionDefaults = super.pluginVersionDefaults ++ List(
    // propVersion(sbtAmmoniteVersion, "ammonite", "0.1.2")
  )

  import VersionUpdateKeys._

  override def projectSettings =
    super.projectSettings ++ deps(tekUserLevelName) ++ pluginVersionDefaults ++
    deps.pluginVersions(tekUserLevelName) ++ Seq(
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
      plugin(trypOrg, "tek-core", TekKeys.tekVersion, "tek-core",
        List("tek/sbt-tek", "core")).bintray("tek"),
      plugin("org.ensime", "sbt-ensime", ensimeVersion,
        "ensime/ensime-sbt").maven,
      plugin("org.scalariform", "sbt-scalariform", scalariformVersion,
        "daniel-trinh/sbt-scalariform").maven,
      plugin(vv, dg, depGraphVersion, s"jrudolph/$dg").maven,
      plugin("com.github.gseitz", "sbt-release", sbtReleaseVersion,
        "sbt/sbt-release").bintray("sbt", "sbt-plugin-releases")
      // ,
      // plugin("com.github.alexarchambault", "sbt-ammonite", sbtAmmoniteVersion,
      //   "alexarchambault/sbt-ammonite").maven
    )
  }

  override def deps: Deps = TekDeps
}
