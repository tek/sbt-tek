package ammonite

import sbt.Attributed._
import sbt.Def.Initialize
import sbt._, Keys._

import scala.util.Try

object AmmonitePlugin extends AutoPlugin {

  object autoImport
  {
    /** Configuration under which Ammonite is run */
    lazy val Ammonite = config("ammonite")

    /** Configuration under which Ammonite is run along with tests */
    lazy val AmmoniteTest = config("ammonite-test")

    val ammoniteVersion = settingKey[String]("Ammonite version")
  }

  import autoImport._

  override def trigger = allRequirements

  override lazy val projectSettings =
    ammoniteSettings(Ammonite, Compile) ++
    ammoniteSettings(AmmoniteTest, Test)

  // Same as Defaults.runTask from SBT, but accepting default arguments too
  def runTask(
    classpath: Initialize[Task[Classpath]],
    mainClassTask: Initialize[Task[Option[String]]],
    scalaRun: Initialize[Task[ScalaRun]],
    defaultArgs: Initialize[Task[Seq[String]]]
  ): Initialize[InputTask[Unit]] = {
    import Def.parserToInput
    val parser = Def.spaceDelimited()
    Def.inputTask {
      val mainClass = mainClassTask.value getOrElse sys.error("No main class detected.")
      val userArgs = parser.parsed
      val args = if (userArgs.isEmpty) defaultArgs.value else userArgs
      scalaRun.value.run(mainClass, data(classpath.value), args, streams.value.log)
    }
  }

  def defaultArgs(initialCommands: String): Seq[String] =
    if (initialCommands.isEmpty)
      Nil
    else
      Seq("--predef", initialCommands)

  def ammoniteSettings(ammoniteConf: Configuration, underlyingConf: Configuration) = inConfig(ammoniteConf)(
    // Getting references to undefined settings when doing ammonite:run without these
    Defaults.compileSettings ++

    // Seems like the class path provided to ammonite:run doesn't take into account the libraryDependencies below
    // without these
    Classpaths.ivyBaseSettings ++

    Seq(
      ammoniteVersion := {
        val fromEnv = sys.env.get("AMMONITE_VERSION")
        def fromProps = sys.props.get("ammonite.version")
        val default = "1.1.0"

        fromEnv
          .orElse(fromProps)
          .getOrElse(default)
      },

      libraryDependencies += "com.lihaoyi" %% "ammonite" % ammoniteVersion.value cross CrossVersion.full,

      configuration := underlyingConf,

      /* Overriding run and runMain defined by compileSettings so that they use fullClasspath of this scope (Ammonite),
       * taking into account the extra libraryDependencies above, and we can also supply default arguments
       * (initialCommands as predef). */
      run := runTask(fullClasspath, mainClass in run, runner in run, (initialCommands in console).map(defaultArgs)).inputTaskValue,
      runMain := Defaults.runMainTask(fullClasspath, runner in run).inputTaskValue,

      mainClass := Some("ammonite.Main"),

      /* Required for the input to be provided to Ammonite */
      connectInput := true
    )
  )
}
