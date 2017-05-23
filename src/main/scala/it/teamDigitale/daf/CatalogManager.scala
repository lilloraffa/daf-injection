package it.teamDigitale.daf

import it.teamDigitale.daf.datastructures.Model.Schema
import it.teamDigitale.daf.schema.schemaMgmt.CoherenceChecker
import it.teamDigitale.daf.utils.TxtFile
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.{Failure, Success, Try}

/**
  * Created by fabiana on 23/05/17.
  */
class CatalogManager() extends Logging{


  def read(uri: String) : Try[Schema] = ???
  def write(schema: Schema) : Boolean = {

    if(schema.operational.std_schema.isDefined) {
      val stdUri = schema.operational.std_schema.get.std_uri

      val checkSchema = for {
        schema2 <- read(stdUri)
        convSchema <- schema.convertToConvSchema()
        stdSchema <- schema2.convertToStdSchema()
      } yield CoherenceChecker.checkCoherenceSchemas(convSchema, stdSchema)


      checkSchema match {
        case Success(value) =>
          logger.info(s"Storing schema having url ${schema.operational.uri}.")
          //salva schema
          value
        case Failure(ex)  =>
          logger.error(s"Unable to write the schema with uri ${schema.operational.uri}. ERROR message: \t ${ex.getMessage}")
          false
      }

    }
    else {
      // inserire senza controlli??
      true
    }
  }


}
