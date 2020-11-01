addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.0-RC3")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.8"

addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.4.0")

addSbtPlugin("com.geirsson" % "sbt-ci-release" % "1.5.3")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.2.0")

addSbtPlugin("com.eed3si9n" % "sbt-projectmatrix" % "0.6.0")

addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.18.0")

addSbtPlugin("com.thesamet" % "sbt-protoc-gen-project" % "0.1.4")
