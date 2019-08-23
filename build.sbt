name := """toggler"""
organization := "org.silvae86"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, SwaggerPlugin)

scalaVersion := "2.12.8"

libraryDependencies += guice

libraryDependencies += filters

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
libraryDependencies += "com.jayway.jsonpath" % "json-path" % "2.4.0"


//JWT authentication support
// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt
libraryDependencies += "io.jsonwebtoken" % "jjwt" % "0.9.1"

// https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-1.2-api
libraryDependencies += "org.apache.logging.log4j" % "log4j-1.2-api" % "2.12.0"

// https://mvnrepository.com/artifact/org.slf4j/slf4j-api
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.26"



