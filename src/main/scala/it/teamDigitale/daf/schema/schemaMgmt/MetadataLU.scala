package it.teamDigitale.daf.schema.schemaMgmt
import it.teamDigitale.daf.utils.JsonMgmt
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import com.typesafe.config.ConfigFactory
import scala.util.{Try, Success, Failure}

object MetadataLU extends Logging {
  val config = ConfigFactory.load()
  val tryMetatdataConfPath = Try(config.getString("Metadata.metadataConfPath"))
  val metadataConf = tryMetatdataConfPath match {
     case Success(x) => {
       val tryMetatdataConf = Try(JsonMgmt.getJson(x))
       tryMetatdataConf match {
         case Success(y) => y
         case Failure(err) => JsonMgmt.getJson("src/main/resources/metadataConf.json")
       }
     }
     case Failure(ex) => JsonMgmt.getJson("src/main/resources/metadataConf.json")
  }
 
  
  def getFieldSeq(nameField: String): Seq[String] = {

    (metadataConf \ nameField).asOpt[Seq[String]] match {
      case Some(s) => s
      case _ => {
        logger.error("Problems with nameField = " + nameField)
        Seq("")
      }
    }

    
  }
}