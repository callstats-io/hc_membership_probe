name := "hc-probe"

version := "0.0.1"

organization := "io.callstats"
      
scalaVersion := "2.11.8"

scalacOptions ++= Seq("-unchecked", "-deprecation")

// https://mvnrepository.com/artifact/javax.servlet/javax.servlet-api
libraryDependencies += "javax.servlet" % "javax.servlet-api" % "3.1.0"

// https://mvnrepository.com/artifact/org.glassfish.jersey.containers/jersey-container-jetty-http
libraryDependencies += "org.glassfish.jersey.containers" % "jersey-container-jetty-http" % "2.25.1"

// https://mvnrepository.com/artifact/org.glassfish.jersey.core/jersey-server
libraryDependencies += "org.glassfish.jersey.core" % "jersey-server" % "2.25.1"

// https://mvnrepository.com/artifact/org.glassfish.jersey.media/jersey-media-json-jackson
libraryDependencies += "org.glassfish.jersey.media" % "jersey-media-json-jackson" % "2.25.1"

// https://mvnrepository.com/artifact/com.fasterxml.jackson.module/jackson-module-scala_2.11
libraryDependencies += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11" % "2.8.7"

// hazelcast client
// https://mvnrepository.com/artifact/com.hazelcast/hazelcast-client
libraryDependencies += "com.hazelcast" % "hazelcast-client" % "3.8.3"

// logging related
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.2"
libraryDependencies += "ch.qos.logback" % "logback-core" % "1.2.2"
libraryDependencies += "net.logstash.logback" % "logstash-logback-encoder" % "4.9"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.6"