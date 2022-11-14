name := "football-notifications"
organization := "dev.maxmelnyk"
version := "0.1.0"
scalaVersion := "2.13.10"

libraryDependencies ++= Dependencies.allDeps

assembly / assemblyJarName := s"${name.value}-assembly.jar"
assembly / test := {} // don't run tests when assembling app
assembly / assemblyMergeStrategy := {
  case "module-info.class" => MergeStrategy.last
  case "local.conf" => MergeStrategy.discard // used for local run
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}
