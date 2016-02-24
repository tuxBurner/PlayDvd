name := """dvdDataBase"""

version := "1.8-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.18",
  "net.coobird" % "thumbnailator" % "0.4.4",
  "commons-io" % "commons-io" % "2.0.1",
  "commons-collections" % "commons-collections" % "3.2.1",
  "commons-lang" % "commons-lang" % "2.6",
  "com.google.code.gson" % "gson" % "2.2.4",
  "com.timgroup" % "jgravatar" % "1.0",
  // RSS FEEDS
  "rome" % "rome" % "1.0",
  // grabbers
  "com.omertron" % "thetvdbapi" % "1.8",
  "com.omertron" % "themoviedbapi" % "4.1",
  "com.omertron" % "traileraddictapi" % "1.5",
  // barcode stuff
  "com.google.zxing" % "core" % "3.2.1",
  // mail
  "com.typesafe.play" %% "play-mailer" % "3.0.1",
  // webjars
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "org.webjars.npm" % "bootstrap" % "3.3.6",

  //"org.webjars.npm" % "jquery" % "2.2.0",
  "org.webjars" % "jquery" % "1.12.0",
  "org.webjars" % "jquerypp" % "1.0b2",

  "org.webjars" % "select2" % "3.5.2",

  "org.webjars.npm" % "font-awesome" % "4.3.0",

  "org.webjars" % "famfamfam-flags" % "0.0",
  "org.webjars.npm" % "holderjs" % "2.8.1",
  "org.webjars.bower" % "hammerjs" % "2.0.6",
  "org.webjars.bower" % "animate.css" % "3.5.1",
  // js i18n stuff
  "org.julienrf" %% "play-jsmessages" % "2.0.0",
  // tuxburners helpers :)
  "com.github.tuxBurner" %% "play-jsannotations" % "2.4.0",
  "com.github.tuxBurner" %% "play-twbs3" % "2.4.0",
  // amazon stuff
  "com.amazonaws" % "aws-java-sdk" % "1.6.10"
)

resolvers ++= Seq(
  "tuxburner.github.io" at "http://tuxburner.github.io/repo",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Neo4j" at "http://m2.neo4j.org/content/repositories/releases/",
  Resolver.sonatypeRepo("snapshots")
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

