name := "Injection Module"
version := "1.0"
scalaVersion := "2.11.8"

lazy val sparkVersion = "1.6.1"
lazy val spark = "org.apache.spark"
//logLevel := Level.Debug

lazy val playVersion = "2.5.0"

libraryDependencies ++= Seq(
  spark %% "spark-core" % sparkVersion,
  spark %% "spark-sql" % sparkVersion,
  spark %% "spark-streaming" % sparkVersion,
  "com.databricks" %% "spark-csv" % "1.5.0",
  
  "com.typesafe" % "config" % "1.0.2",
  
  //JSON Library
  "com.typesafe.play" %% "play-json" % playVersion
    exclude("com.fasterxml.jackson.core", "jackson-databind")
  
)

// Change this to another test framework if you prefer
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.1" % "test"

//MongoDB
libraryDependencies += "org.mongodb.scala" %% "mongo-scala-driver" % "2.0.0"

// Log4j
libraryDependencies += "org.apache.logging.log4j" % "log4j-api" % "2.8.2"
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.8.2"
libraryDependencies += "org.apache.logging.log4j" %% "log4j-api-scala" % "2.8.2"

// Uncomment to use Akka
//libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.11"
