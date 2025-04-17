lazy val root = (project in file("."))
  .enablePlugins(PlayJava, PlayEbean)
  .settings(
    name := """dvdDataBase""",
    version := "1.14-SNAPSHOT",
    crossScalaVersions := Seq("2.13.15", "3.3.3"),
    scalaVersion := crossScalaVersions.value.head,
    libraryDependencies ++= Seq(
      guice,
      ehcache,
      ws,
      "mysql" % "mysql-connector-java" % "8.0.33",

      "net.coobird" % "thumbnailator" % "0.4.20",

      "commons-io" % "commons-io" % "2.18.0",
      "commons-collections" % "commons-collections" % "3.2.2",
      "org.apache.commons" % "commons-lang3" % "3.17.0",

      "com.google.code.gson" % "gson" % "2.2.4",

      "com.timgroup" % "jgravatar" % "1.2",

      // RSS FEEDS
      // TODO: https://mvnrepository.com/artifact/com.rometools/rome
      "rome" % "rome" % "1.0",

      // grabbers
      "com.omertron" % "thetvdbapi" % "1.9",
      "com.omertron" % "themoviedbapi" % "4.3",
      "com.omertron" % "traileraddictapi" % "1.5",

      // barcode stuff
      "com.google.zxing" % "core" % "3.3.2",

      // mail
      "com.typesafe.play" %% "play-mailer" % "9.1.0",
      "com.typesafe.play" %% "play-mailer-guice" % "9.1.0",

      // webjars
      "org.webjars" %% "webjars-play" % "2.9.1",
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
      "org.julienrf" %% "play-jsmessages" % "6.0.0",

      // tuxburners helpers :)
      "com.github.tuxBurner" %% "play-jsannotations" % "2.9.0-SNAPSHOT",
      //,."com.github.tuxBurner" %% "play-twbs3" % "2.4.0",
      "com.github.tuxBurner" %% "play-akkajobs" % "2.9.0-SNAPSHOT",
      // amazon stuff
      "com.amazonaws" % "aws-java-sdk" % "1.11.311",

      // for nice and smooth html parsing
      "org.jodd" % "jodd-http" % "5.0.12",
      "org.jodd" % "jodd-lagarto" % "5.0.12"
    ),
    javacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-parameters",
      "-Xlint:unchecked",
      "-Xlint:deprecation"
      //,"-Werror"
    ),
)
//#play-ebean-models
//Compile / playEbeanModels := Seq("models.*")
//#play-ebean-debug
playEbeanDebugLevel := 9
//#play-ebean-debug

//playEbeanAgentArgs += ("detectPropertyAccess" -> "true")

