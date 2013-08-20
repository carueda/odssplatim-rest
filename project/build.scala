import sbt._
import Keys._
import org.scalatra.sbt._
import sbtassembly.Plugin._
import AssemblyKeys._


object OdssPlatformTimelineBuild extends Build {

  import com.earldouglas.xsbtwebplugin.PluginKeys.port

  val Organization      = "org.mbari"
  val Name              = "ODSS Platform Timeline"
  val Version           = "0.1.0-SNAPSHOT"
  val ScalaVersion      = "2.10.0"
  val ScalatraVersion   = "2.2.1"     //  "2.2.2-SNAPSHOT"  --> resolvers += Opts.resolver.sonatypeSnapshots,


  def container = config("container")
  val jettyPort = 8181

  lazy val project = Project(
    "odss-platform-timeline",
    file("."),
    settings = Defaults.defaultSettings ++ ScalatraPlugin.scalatraWithJRebel ++ assemblySettings ++ Seq(
      organization := Organization,
      name := Name,
      version := Version,
      scalaVersion := ScalaVersion,
      resolvers += Classpaths.typesafeReleases,
      libraryDependencies ++= Seq(
        "org.mongodb"                   %% "casbah"                      % "2.6.0",
        "org.scalatra"                  %% "scalatra"                    % ScalatraVersion,
        "com.typesafe"                   % "config"                      % "1.0.2",
        "org.scalatra"                  %% "scalatra-specs2"             % ScalatraVersion % "test",
        "org.scalatra"                  %% "scalatra-json"               % ScalatraVersion,
        "org.scalatra"                  %% "scalatra-swagger"            % ScalatraVersion,
        "org.json4s"                    %% "json4s-native"               % "3.2.4",
        "com.github.nscala-time"        %% "nscala-time"                 % "0.4.0",
        "com.typesafe"                  %% "scalalogging-slf4j"          % "1.0.1",

        // if creating standalone, uncomment code in JettyLauncher and set "container;compile" here:
        "org.eclipse.jetty"              % "jetty-webapp"                % "8.1.12.v20130726" % "container",

        "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container;provided;test" artifacts Artifact("javax.servlet", "jar", "jar")
      ),
      scalacOptions ++= Seq("-feature"),
      port in container := jettyPort,

      // assembly:
      jarName in assembly := "odssplatim.jar",
      mainClass in assembly := Some("org.mbari.odss.services.timeline.JettyLauncher"),
      test in assembly := {},
      mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) => {
        case "about.html"     => MergeStrategy.discard
        case x => old(x)
      }}
    )
  ).settings(net.virtualvoid.sbt.graph.Plugin.graphSettings: _*)
}
