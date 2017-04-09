package tryp

import java.nio.file.Paths

import util.Try

import sbt._
import Keys._
import xsbti.{Position, Maybe}

import org.ensime.EnsimeKeys.ensimeIgnoreMissingDirectories
import org.ensime.EnsimeCoursierKeys.ensimeServerVersion

import com.typesafe.sbt.SbtScalariform.autoImport._

import sbtrelease.ReleasePlugin.autoImport._
import ReleaseTransformations._
import sbtrelease.Version.Bump

import coursier.Keys._

import TekKeys._

object Tek
extends AutoPlugin
with Tryplug
{
  override def requires = plugins.JvmPlugin
  override def trigger = allRequirements

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

  override def buildSettings = Seq(
    ensimeServerVersion := "2.0.0-SNAPSHOT"
  )

  override def projectSettings = Seq(
    bintrayTekResolver,
    bintrayPluginResolver("pfn"),
    scalariformFormat in Compile := Nil,
    scalariformFormat in Test := Nil,
    releaseProc,
    releaseIgnoreUntrackedFiles := true,
    ensimeIgnoreMissingDirectories := true,
    TrypKeys.useCoursier := true,
    coursierUseSbtCredentials := true,
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
    splain := true,
    splainBreakInfix := 100,
    libraryDependencies ++= (if (splain.value) List(compilerPlugin("tryp" %% "splain" % "0.1.23")) else Nil),
    scalacOptions ++= (
      if (splain.value) List("-P:splain:bounds", s"-P:splain:breakinfix:${splainBreakInfix.value}")
      else Nil
    )
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
