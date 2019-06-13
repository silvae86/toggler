name := """toggler"""
organization := "org.silvae86"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, SwaggerPlugin)

scalaVersion := "2.12.8"

libraryDependencies += guice

// https://mvnrepository.com/artifact/io.swagger/swagger-play2
libraryDependencies += "io.swagger" %% "swagger-play2" % "1.7.1"

libraryDependencies += "org.webjars" % "swagger-ui" % "2.2.0"
libraryDependencies += "org.mongodb.morphia" % "morphia" % "1.2.1"
libraryDependencies += "org.mockito" % "mockito-core" % "2.10.0" % "test"

swaggerDomainNameSpaces := Seq("models")






