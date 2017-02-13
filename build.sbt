name := """dvdDataBase"""

version := "1.9-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava,PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "net.coobird" % "thumbnailator" % "0.4.8",
  "commons-io" % "commons-io" % "2.0.1",
  "commons-collections" % "commons-collections" % "3.2.1",
  "commons-lang" % "commons-lang" % "2.6",
  "com.google.code.gson" % "gson" % "2.2.4",
  "com.timgroup" % "jgravatar" % "1.0",
  // RSS FEEDS
  "rome" % "rome" % "1.0",
  // grabbers
  "com.omertron" % "thetvdbapi" % "1.9",
  "com.omertron" % "themoviedbapi" % "4.3",
  "com.omertron" % "traileraddictapi" % "1.5",
  // barcode stuff
  "com.google.zxing" % "core" % "3.3.0",
  // mail
  "com.typesafe.play" %% "play-mailer" % "5.0.0",
  // webjars
  "org.webjars" %% "webjars-play" % "2.5.0",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.webjars" % "jquery" % "1.12.4",
  "org.webjars" % "jquerypp" % "1.0b2",
  "org.webjars" % "select2" % "3.5.2",
  "org.webjars" % "font-awesome" % "4.7.0",
  "org.webjars" % "famfamfam-flags" % "0.0",
  "org.webjars" % "holderjs" % "2.5.2",
  "org.webjars" % "hammerjs" % "2.0.6",
  "org.webjars" % "animate.css" % "3.5.2",
  // js i18n stuff
  "org.julienrf" %% "play-jsmessages" % "2.0.0",
  // tuxburners helpers :)
  "com.github.tuxBurner" %% "play-jsannotations" % "2.5.0",
  "com.github.tuxBurner" %% "play-twbs3" % "2.4.0",
  "com.github.tuxBurner" %% "play-akkajobs" % "1.0.1",
  // amazon stuff
  "com.amazonaws" % "aws-java-sdk" % "1.6.10"
)

resolvers ++= Seq(
  "tuxburner.github.io" at "http://tuxburner.github.io/repo",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Neo4j" at "http://m2.neo4j.org/content/repositories/releases/",
  Resolver.sonatypeRepo("snapshots")
)
