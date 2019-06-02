import com.typesafe.sbt.packager.docker.ExecCmd

name := """dvdDataBase"""

version := "1.12"

lazy val root = (project in file(".")).enablePlugins(PlayJava,PlayEbean)

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  ehcache,
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
  "com.google.zxing" % "core" % "3.3.2",

  // mail
  "com.typesafe.play" %% "play-mailer" % "6.0.1",
  "com.typesafe.play" %% "play-mailer-guice" % "6.0.1",

  // webjars
  "org.webjars" %% "webjars-play" % "2.6.3",
  "org.webjars" % "bootstrap" % "2.3.2",
  "org.webjars" % "jquery" % "1.11.2",
  "org.webjars" % "jquerypp" % "1.0b2",
  "org.webjars" % "select2" % "3.5.2",
  "org.webjars" % "font-awesome" % "3.2.1",
  "org.webjars" % "famfamfam-flags" % "0.0",
  "org.webjars" % "holderjs" % "2.4.0",
  "org.webjars" % "hammerjs" % "2.0.3-1",
  "org.webjars" % "animate.css" % "3.2.0",

  // js i18n stuff
  "org.julienrf" %% "play-jsmessages" % "3.0.0",

  // tuxburners helpers :)
  "com.github.tuxBurner" %% "play-jsannotations" % "2.6.0",
  //,."com.github.tuxBurner" %% "play-twbs3" % "2.4.0",
  "com.github.tuxBurner" %% "play-akkajobs" % "2.6.1",
  // amazon stuff
  "com.amazonaws" % "aws-java-sdk" % "1.11.311",

  // for nice and smooth html parsing
  "org.jodd" % "jodd-http" % "5.0.12",
  "org.jodd" % "jodd-lagarto" % "5.0.12"

)

resolvers ++= Seq(
  "tuxburner.github.io" at "http://tuxburner.github.io/repo",
  "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
  "Neo4j" at "http://m2.neo4j.org/content/repositories/releases/",
  Resolver.sonatypeRepo("snapshots")
)

// http://www.scala-sbt.org/sbt-native-packager/formats/docker.html
// docker infos go here
maintainer in Docker := "Sebastian Hardt"
packageName in Docker := "tuxburner/playdvd"
dockerExposedPorts in Docker := Seq(9000)
dockerExposedVolumes in Docker := Seq("/data")

// add the command to use deadzone roster conf
dockerCommands ++= Seq(
  ExecCmd("CMD", "-Dconfig.file=/config/playdvd.conf")
)


dockerUpdateLatest in Docker := true
