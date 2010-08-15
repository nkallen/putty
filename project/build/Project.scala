import sbt._

class Project(info: ProjectInfo) extends DefaultProject(info) {
  val netty = "org.jboss.netty" % "netty" % "3.2.1.Final" from "http://repository.jboss.org/nexus/content/groups/public/org/jboss/netty/netty/3.2.1.Final/netty-3.2.1.Final.jar"
}

