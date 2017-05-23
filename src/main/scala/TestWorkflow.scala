import com.typesafe.config.ConfigFactory
import it.teamDigitale.daf.datamanagers.DataManager
import it.teamDigitale.daf.schema.schemaMgmt.CoherenceChecker
import org.apache.logging.log4j.scala.Logging

import scala.util.{Failure, Success}

/**
  * Created by fabiana on 22/05/17.
  */
object TestWorkflow extends App with Logging{

  val uri = ConfigFactory.load().getString("WebServices.catalogUrl")

  val dm = new DataManager(uri)
  val tryschema = dm.getSchemaFromUri("1")

  val convSchema = for{
    schema <- tryschema
    convSchema <- schema.convertToConvSchema()
  } yield convSchema

  convSchema match {
    case Success(schema) => println(schema)
    case Failure(ex) => println(ex.getStackTrace.mkString("\n\t"))
  }

  val stdSchema = for{
    schema <- tryschema
    convSchema <- schema.convertToStdSchema()
  } yield convSchema

  stdSchema match {
    case Success(schema) => println(schema)
    case Failure(ex) =>
      logger.error(s"ERROR ${ex.getMessage}")
      logger.error(ex.getStackTrace.mkString("\n\t"))
  }


}
