import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

    val appName         = "dvdDataBase"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(

      javaCore, javaJdbc, javaEbean,

      "mysql" % "mysql-connector-java" % "5.1.18",      
      "net.coobird" % "thumbnailator" % "0.4.1",
      "commons-io" % "commons-io" % "2.0.1",
      "commons-collections" % "commons-collections" % "3.2.1",
      "com.google.code.gson" % "gson" % "2.2.1",
      "com.github.ralfebert" % "jgravatar" % "1.0-SNAPSHOT",

      "com.omertron" % "thetvdbapi" % "1.6-SNAPSHOT",
      "com.omertron" % "themoviedbapi" % "3.4-SNAPSHOT",

      "com.google.zxing" % "core" % "2.1",

      "com.typesafe" %% "play-plugins-mailer" % "2.1.0",
      
      "org.webjars" % "webjars-play" % "0.1",
      "org.webjars" % "bootstrap" % "2.2.2",
      "org.webjars" % "jquery" % "1.8.2",
      "org.webjars" % "jquerypp" % "1.0b2-1",
      "org.webjars" % "select2" % "3.2",
      "org.webjars" % "font-awesome" % "3.0.0"
    )


    val main = play.Project(appName, appVersion, appDependencies).settings(
      resolvers += ("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"),
      resolvers += ("webjars" at "http://webjars.github.com/m2")
    )
    
}
