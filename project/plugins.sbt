resolvers += Resolver.url(
  "bintray-tek-sbt",
  url("https://dl.bintray.com/tek/sbt-plugins")
)(Resolver.ivyStylePatterns)

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

val pattern = "[organisation]/[module]/[revision]/[artifact]-[revision]" +
  "(-[timestamp]).[ext]"

val nexusUri = "https://nexus.ternarypulsar.com/nexus/content/repositories"

resolvers += 
  Resolver.url("pulsar", url(s"$nexusUri/releases"))(Patterns(pattern))
