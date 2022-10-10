val tapirVersion = "1.1.2"

lazy val rootProject = (project in file(".")).settings(
  Seq(
    name := "jsonschema-server",
    version := "0.1.0-SNAPSHOT",
    organization := "com.dkovalenko",
    scalaVersion := "2.13.9",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server-zio" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-http4s-server" % tapirVersion,
      "org.http4s" %% "http4s-blaze-server" % "0.23.12",
      "com.softwaremill.sttp.tapir" %% "tapir-swagger-ui-bundle" % tapirVersion,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe" % tapirVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.3",
      "com.github.etaty" %% "rediscala" % "1.9.0",
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-stub-server" % tapirVersion % Test,
      "dev.zio" %% "zio-test" % "2.0.0" % Test,
      "dev.zio" %% "zio-test-sbt" % "2.0.0" % Test,
      "com.softwaremill.sttp.client3" %% "circe" % "3.8.2" % Test
    ),
    reStart / mainClass := Some("com.dkovalenko.Main"),
    testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
  )
)
