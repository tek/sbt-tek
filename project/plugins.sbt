resolvers += Resolver.url(
  "bintray-tek-sbt",
  url("https://dl.bintray.com/tek/sbt-plugins")
)(Resolver.ivyStylePatterns)

libraryDependencies += Defaults.sbtPluginExtra(
  "io.tryp" % "tryplug" % P.autoImport.tryplugVersion.value,
  (sbtBinaryVersion in update).value,
  (scalaBinaryVersion in update).value
)

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.8")
