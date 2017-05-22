name := "Injection Module"
version := "0.1.0"
organization := "it.teamdigitale"

scalaVersion := "2.11.8"


resolvers ++= Seq(
  "Cloudera Repository" at "https://repository.cloudera.com/artifactory/cloudera-repos/"
)

lazy val sparkVersion = "2.1.0.cloudera1"
lazy val spark = "org.apache.spark"
lazy val jacksonVersion = "2.8.8"
lazy val playVersion = "2.5.0"
//logLevel := Level.Debug

def dependencyToProvide(scope: String = "compile") = Seq(
  spark %% "spark-core" % sparkVersion % scope exclude("com.fasterxml.jackson.core", "jackson-databind"),
  spark %% "spark-sql" % sparkVersion % scope exclude("com.fasterxml.jackson.core", "jackson-databind"),
  spark %% "spark-streaming" % sparkVersion % scope exclude("com.fasterxml.jackson.core", "jackson-databind")
)

libraryDependencies ++= Seq(

  "com.databricks" %% "spark-csv" % "1.5.0",
  "com.typesafe" % "config" % "1.0.2",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.specs2" %% "specs2-mock" % "3.8.9" % "test",
  "org.specs2" %% "specs2-core" % "3.8.9" % "test",
  //JSON Library
  "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
  //"com.typesafe.play" %% "play-json" % playVersion exclude("com.fasterxml.jackson.core", "jackson-databind")
  
) ++ dependencyToProvide()

// Log4j
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.8.2"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.8.2"
libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "2.8.2"
