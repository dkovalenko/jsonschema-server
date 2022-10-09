ThisBuild / scalacOptions ++=
  Seq(
    "-deprecation",
    "-feature",
    "-language:implicitConversions",
    "-unchecked",
    "-Xfatal-warnings",
    "-Wunused:imports",
    // "-Yexplicit-nulls", // experimental (I've seen it cause issues with circe)
  )
// Seq("-new-syntax", "-rewrite")
