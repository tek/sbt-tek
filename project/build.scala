package tryp

import sbt._
import sbt.Keys._

import com.typesafe.sbt.SbtScalariform.autoImport._

import TrypKeys._
import VersionUpdateKeys._

object TekKeys
{
  val scalariformVersion = settingKey[String]("scalariform version")
}
import TekKeys._

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

  lazy val root = pluginSubProject("root")
    .in(file("."))
    .settings(
      publish := (),
      publishLocal := (),
      versionUpdater := {
        new Versions {
          def projectDir =
            Option(VersionUpdateKeys.projectDir.value / "project")
          override def handlePrefix = "P."
        }
      }
    )
    .aggregate(core)

  val wantDevdeps = false

  object TekDeps
  extends Deps
  {
    override def deps = super.deps ++ Map(
      "core" → core
    )

    val tryplug = pd("tryp.sbt", "tryplug", tryplugVersion, "tek",
      "tek/tryplug", "tryplug", "macros")

    val scalariform = pd("org.scalariform", "sbt-scalariform",
      scalariformVersion, "daniel-trinh", "sbt-scalariform")

    val core = ids(tryplug, scalariform)

    val root = ids(tryplug)
  }

  override def deps = TekDeps
}
