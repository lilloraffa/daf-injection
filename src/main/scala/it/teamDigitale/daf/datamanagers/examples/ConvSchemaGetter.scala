package it.teamDigitale.daf.datamanagers.examples

import it.teamDigitale.daf.datastructures.ConvSchema
import it.teamDigitale.daf.datastructures.Model.{DatasetSchema, DcatapitInfo, OperationalInfo, Schema}
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging

import scala.util.{Failure, Success, Try}

/**
  * This class is just for test.
  * Generate the ConvSchema extracted from json files in /resources/stdSchema
  */
object ConvSchemaGetter extends SchemaGetter[ConvSchema] with Logging{

  def getSchema(): Option[ConvSchema] = {

    val stream_operational = getClass.getResourceAsStream("/dataschema/data-operational.json")
    val stream_dcatap = getClass.getResourceAsStream("/dataschema/data-dcatapit.json")
    val stream_dataschema = getClass.getResourceAsStream("/dataschema/data-dataschema.json")

    val classDataSchema = Try(JsonConverter.fromJson[DatasetSchema](stream_dataschema))
    val classDataDcatapit = Try(JsonConverter.fromJson[DcatapitInfo](stream_dcatap))
    val classDataOperational = Try(JsonConverter.fromJson[OperationalInfo](stream_operational))

    val res: Try[Schema] = for {
      ds <- classDataSchema
      dcatapitInfo <- classDataDcatapit
      operationalInfo <- classDataOperational
    } yield Schema(ds, dcatapitInfo, operationalInfo)

    res match {
      case Success(schema) => Some(schema.convertToConvSchema())
      case Failure(ex) => logger.error(ex.getMessage)
        None
    }

  }
}