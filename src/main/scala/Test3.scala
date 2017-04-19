import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import com.typesafe.config.ConfigFactory

object Test3 extends App with Logging{
  println("Test Logger")
  logger.error("Doing stuff")
  
  var config = ConfigFactory.load()
  println(config.getString("Inj-properties.sftpBasePath"))
  
  
}