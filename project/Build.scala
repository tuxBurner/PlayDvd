import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "dvdDataBase"
    val appVersion      = "1.4-SNAPSHOT"

    val appDependencies = Seq(

      javaCore, 
      javaJdbc, 
      javaEbean, 
      cache,

      "mysql" % "mysql-connector-java" % "5.1.18",      
      "net.coobird" % "thumbnailator" % "0.4.4",

      "commons-io" % "commons-io" % "2.0.1",
      "commons-collections" % "commons-collections" % "3.2.1",
      "commons-lang" % "commons-lang" % "2.6",


      "com.google.code.gson" % "gson" % "2.2.4",
      "com.timgroup" % "jgravatar" % "1.0",
      /* RSS FEEDS */
      "rome" % "rome" % "1.0",

      "com.omertron" % "thetvdbapi" % "1.6",
      "com.omertron" % "themoviedbapi" % "3.8",
      "com.omertron" % "traileraddictapi" % "1.4",

      "com.google.zxing" % "core" % "2.3.0",

      "com.typesafe" %% "play-plugins-util" % "2.2.0",
      "com.typesafe" %% "play-plugins-mailer" % "2.2.0",

      "org.webjars" % "webjars-play_2.10" % "2.2.0",
      "org.webjars" % "bootstrap" % "2.3.2",
      "org.webjars" % "jquery" % "1.9.1",
      "org.webjars" % "jquerypp" % "1.0b2-1",
      "org.webjars" % "select2" % "3.3.1",
      "org.webjars" % "font-awesome" % "3.2.1",
      "org.webjars" % "famfamfam-flags" % "0.0",

      "com.github.julienrf" %% "play-jsmessages" % "1.5.1", 
      "com.github.tuxBurner" %% "play-jsannotations" % "1.2.0",

      "com.amazonaws" % "aws-java-sdk" % "1.6.10"
    )


    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers += ("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"),
      resolvers += ("webjars" at "http://webjars.github.com/m2"),
      resolvers += "tuxburner.github.io" at "http://tuxburner.github.io/repo",
      resolvers += "julienrf.github.com" at "http://julienrf.github.com/repo/"
    )
    
}
