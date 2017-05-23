import com.typesafe.config.ConfigFactory
import it.teamDigitale.daf.schema.schemaMgmt.{CoherenceChecker, SchemaManager}
import org.apache.logging.log4j.scala.Logging

import scala.util.{Failure, Success}

/**
  * Created by fabiana on 22/05/17.
  */
object TestWorkflow extends App with Logging{

  val uri = ConfigFactory.load().getString("WebServices.catalogUrl")

  val dm = new SchemaManager(uri)
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
      //if the stdSchema does not contains the uri throws an exception
      logger.info(s"ERROR ${ex.getMessage}")
      logger.info(ex.getStackTrace.mkString("\n\t"))
  }




}
