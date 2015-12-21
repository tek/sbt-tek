libraryDependencies += Defaults.sbtPluginExtra(
  "tryp.sbt" % "tryplug" % P.tryplugVersion.value,
  (sbtBinaryVersion in update).value,
  (scalaBinaryVersion in update).value
)
libraryDependencies += Defaults.sbtPluginExtra(
  "org.scalariform" % "sbt-scalariform" % P.scalariformVersion.value,
  (sbtBinaryVersion in update).value,
  (scalaBinaryVersion in update).value
)
