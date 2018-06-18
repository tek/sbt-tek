package tryp

import java.nio.file.Paths

import util.Try

import sbt._
import Keys._
// import xsbti.{Position, Maybe}

import org.ensime.EnsimeKeys.{ensimeIgnoreMissingDirectories, ensimeServerVersion, ensimeIgnoreScalaMismatch}

import coursier.Keys._

import TekKeys._

object Tek
extends AutoPlugin
with Tryplug
{
  override def trigger = allRequirements

  def majorPlugins = List("tryp", "tryplug", "tek")

  // def posMapper(base: String)(pos: Position) = {
  //   Try {
  //     val sourcePath =
  //       if (pos.sourcePath.isDefined) Some(pos.sourcePath.get)
  //       else None
  //     sourcePath.flatMap { sp =>
  //       val relative = Paths.get(base).relativize(Paths.get(sp))
  //       if (relative.startsWith("..")) None
  //       else Some(new Position {
  //           def line = pos.line
  //           def lineContent = pos.lineContent
  //           def offset = pos.offset
  //           def pointer = pos.pointer
  //           def pointerSpace = pos.pointerSpace
  //           def sourceFile = pos.sourceFile
  //           def sourcePath = Maybe.just(relative.toString)
  //         })
  //     }
  //   } getOrElse None
  // }

  override def buildSettings = Seq(
    ensimeServerVersion := "2.0.0-SNAPSHOT"
  )

  override def projectSettings = Seq(
    bintrayTekResolver,
    bintrayPluginResolver("pfn"),
    ensimeIgnoreMissingDirectories := true,
    coursierUseSbtCredentials := true,
    TekKeys.trypArtifactRepo := true,
    resolvers ++= (if (TekKeys.trypArtifactRepo.value) pulsarResolvers else Nil),
    publishTo := publishTo.value orElse {
      val repo = if (isSnapshot.value) "snapshots" else "releases"
      Some(repo at s"$pulsarUri/$repo")
    },
    // sourcePositionMappers += posMapper((baseDirectory in ThisBuild).value.toString) _,
    resolvers += Resolver.bintrayRepo("tek", "maven"),
    splain := true,
    splainBreakInfix := 100,
    splainTruncRefined := 20,
    splainVersion := splainVersion.?(_.getOrElse("0.3.0")).value,
    libraryDependencies ++= (
      if (splain.value) List(compilerPlugin("io.tryp" % "splain" % splainVersion.value cross CrossVersion.patch))
      else Nil
    ),
    scalacOptions ++= (
      if (splain.value)
        List(
          "-P:splain:bounds",
          s"-P:splain:breakinfix:${splainBreakInfix.value}",
          "-P:splain:boundsimplicits:false",
          "-P:splain:compact",
          s"-P:splain:truncrefined:${splainTruncRefined.value}"
        )
      else Nil
    )
  )

  def nexusPulsar = "nexus.ternarypulsar.com"

  def pulsarUri = nexusUri(nexusPulsar)

  lazy val pulsarResolvers = List("snapshots", "releases").flatMap { tpe =>
    val ivy = Resolver.url(s"pulsar-ivy-$tpe", url(s"$pulsarUri/$tpe"))(Patterns(true, nexusPattern))
    val maven = MavenRepository(s"pulsar-maven-$tpe", s"$pulsarUri/$tpe")
    List(ivy, maven)
  }
}
