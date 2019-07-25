name := """toggler"""
organization := "org.silvae86"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, SwaggerPlugin)

scalaVersion := "2.12.8"

libraryDependencies += guice

// https://mvnrepository.com/artifact/io.swagger/swagger-play2
libraryDependencies += "io.swagger" %% "swagger-play2" % "1.7.1"

libraryDependencies += "org.webjars" % "swagger-ui" % "2.2.0"

// https://mvnrepository.com/artifact/dev.morphia.morphia/core
libraryDependencies += "dev.morphia.morphia" % "core" % "1.5.2"

libraryDependencies += "org.mockito" % "mockito-core" % "2.10.0" % "test"

// https://mvnrepository.com/artifact/org.projectlombok/lombok
libraryDependencies += "org.projectlombok" % "lombok" % "1.18.8" % "provided"

swaggerDomainNameSpaces := Seq("models")

// https://mvnrepository.com/artifact/org.mindrot/jbcrypt
libraryDependencies += "org.mindrot" % "jbcrypt" % "0.4"

libraryDependencies += "org.pac4j" % "pac4j" % "3.7.0"
libraryDependencies += "org.pac4j" % "pac4j-http" % "3.7.0"

//JWT authentication support
libraryDependencies += "com.jason-goodwin" %% "authentikat-jwt" % "0.4.5"


