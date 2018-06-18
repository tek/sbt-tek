val core = pluginSubProject("core")
  .settings(
    libs := TekLibs,
    name := "tek-core"
  )

lazy val userlevelBuild = pluginSubProject("user-level-build")
  .settings(
    libs := TekLibs,
    name := "tek-user-level-build"
  )
  .dependsOn(core)

lazy val userlevel = pluginSubProject("userlevel", Some("user-level"))
  .settings(
    libs := TekLibs,
    name := "tek-user-level"
  )
  .dependsOn(core)

lazy val root = pluginProject("root")
  .settings(
    libs := TekLibs,
    releaseIgnoreUntrackedFiles := true,
    handlePrefixMap := Map(
      baseDirectory.value -> "tryp.TekBuildKeys."
    )
  )
  .aggregate(core, userlevel, userlevelBuild)
