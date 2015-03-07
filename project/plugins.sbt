// Android development and IntelliJ IDEA integration

addSbtPlugin("com.hanhuy.sbt" % "android-sdk-plugin" % "1.3.11")

addSbtPlugin("com.hanhuy.sbt" % "sbt-idea" % "1.7.0-SNAPSHOT")

// Scoverage and Coveralls

resolvers += Resolver.sbtPluginRepo("snapshots")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "1.0.4")

addSbtPlugin("org.scoverage" %% "sbt-coveralls" % "1.0.0.BETA1")
