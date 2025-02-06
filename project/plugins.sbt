// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.9.6")

// Web plugins
addSbtPlugin("com.github.sbt" % "sbt-less" % "2.0.1")
addSbtPlugin("com.github.sbt" % "sbt-jshint" % "2.0.1")



// Play enhancer - this automatically generates getters/setters for public fields
// and rewrites accessors of these fields to use the getters/setters. Remove this
// plugin if you prefer not to have this feature, or disable on a per project
// basis using disablePlugins(PlayEnhancer) in your build.sbt
//addSbtPlugin("com.typesafe.sbt" % "sbt-play-enhancer" % "1.2.2")

// Play Ebean support, to enable, uncomment this line, and enable in your build.sbt using
// enablePlugins(PlayEbean).
addSbtPlugin("com.typesafe.play" % "sbt-play-ebean" % "7.3.0")
