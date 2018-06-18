import sbt._

object P
extends AutoPlugin
{
  object autoImport
  {
    val tryplugVersion = settingKey[String]("tryplug version")
  }
}
