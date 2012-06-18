import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "dvdDataBase"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
      "mysql" % "mysql-connector-java" % "5.1.18",
      "com.github.savvasdalkitsis" % "jtmdb" % "1.0.0",
      "net.coobird" % "thumbnailator" % "0.4.1",
      "commons-io" % "commons-io" % "2.0.1",
      "commons-collections" % "commons-collections" % "3.2.1",
      "com.github.twitter" % "bootstrap" % "2.0.3",
      "com.jquery" % "jquery" % "1.7.1",
      "com.google.code.gson" % "gson" % "2.2.1",
      "com.github.ralfebert" % "jgravatar" % "1.0-SNAPSHOT"
    )

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
      resolvers += ("Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"),
      resolvers += ("webjars" at "http://webjars.github.com/m2")
    )

}
