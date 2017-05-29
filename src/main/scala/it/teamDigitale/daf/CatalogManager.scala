package it.teamDigitale.daf

import it.gov.daf.catalogmanagerclient.model.MetaCatalog
import it.teamDigitale.daf.schemamanager.CoherenceChecker
import it.teamDigitale.daf.utils.TxtFile
import org.apache.logging.log4j.scala.Logging
import org.apache.spark.{SparkConf, SparkContext}
import it.teamDigitale.daf.datastructures._

import scala.util.{Failure, Success, Try}

/**
  * Created by fabiana on 23/05/17.
  */
class CatalogManager() extends Logging{


  def read(uri: String) : Try[MetaCatalog] = ???
  def write(schema: MetaCatalog) : Boolean = {

    if(schema.operational.stdSchema == 1 ) {
      val stdUri = schema.operational.stdSchema.stdUri

      val checkSchema = for {
        schema2 <- read(stdUri)
        convSchema <- convertToConvSchema(schema)
        stdSchema <- convertToStdSchema(schema2)
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
