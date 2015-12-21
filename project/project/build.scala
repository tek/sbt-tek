import sbt._

object P
extends Plugin
{
  val tryplugVersion = settingKey[String]("tryplug version")
  val scalariformVersion = settingKey[String]("scalariform version")
}
