logLevel := Level.Warn

resolvers += "Kamon Releases" at "http://repo.kamon.io"

addSbtPlugin("org.rbayer" % "grunt-sbt" % "1.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.8.0")

addSbtPlugin("com.github.sdb" % "xsbt-filter" % "0.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-aspectj" % "0.9.4")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")
