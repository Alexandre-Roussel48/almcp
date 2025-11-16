val scala3Version = "3.7.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "almcp",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies += "dev.zio" %% "zio" % "2.1.0",
    libraryDependencies += "dev.zio" %% "zio-streams" % "2.1.0",
    libraryDependencies += "dev.zio" %% "zio-test" % "2.1.0" % Test,
    libraryDependencies += "dev.zio" %% "zio-test-sbt" % "2.1.0" % Test,
    libraryDependencies += "io.circe" %% "circe-core" % "0.14.9",
    libraryDependencies += "io.circe" %% "circe-generic" % "0.14.9",
    libraryDependencies += "io.circe" %% "circe-parser"  % "0.14.9",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
  )
