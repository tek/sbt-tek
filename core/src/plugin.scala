package tryp

import sbt._
import Keys._

import com.typesafe.sbt.SbtScalariform.autoImport._

object TekKeys
{
  import TrypKeys.Tryp
  val tekVersion = settingKey[String]("sbt-tek version") in Tryp
  val ensimeVersion = settingKey[String]("ensime version") in Tryp
  val scalariformVersion = settingKey[String]("scalariform version") in Tryp
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
    updater.update(pspec("tek", "sbt-tek", TekKeys.tekVersion))
  }

  override def projectSettings =
    super.projectSettings ++ deps(tekUserLevelName)

  object TekDeps
  extends Deps
  {
    override def deps = super.deps ++ Map(
      userLevelName → userLevel
    )

    val huy = "com.hanhuy.sbt"
    val sdkName = "android-sdk-plugin"
    val protifyName = "protify"

    val userLevel = ids(
      pd(trypOrg, "tryplug", TrypKeys.tryplugVersion, "tek", "tek/tryplug",
        "tryplug", "macros"),
      pd("org.ensime", "ensime-sbt", ensimeVersion, "ensime",
        "ensime/ensime-sbt"),
      pd("org.scalariform", "sbt-scalariform",
        scalariformVersion, "daniel-trinh", "sbt-scalariform")
    )
  }

  override def deps: Deps = TekDeps
}
