package it.teamDigitale.daf.datamanagers.examples

import it.gov.daf.catalogmanagerclient.model.{DatasetCatalog, DcatApIt, MetaCatalog, Operational}
import it.teamDigitale.daf.datastructures.ConvSchema
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging
import it.teamDigitale.daf.datastructures._
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

    val classDataSchema = Try(JsonConverter.fromJson[DatasetCatalog](stream_dataschema))
    val classDataDcatapit = Try(JsonConverter.fromJson[DcatApIt](stream_dcatap))
    val classDataOperational = Try(JsonConverter.fromJson[Operational](stream_operational))

    val res: Try[MetaCatalog] = for {
      ds <- classDataSchema
      dcatapitInfo <- classDataDcatapit
      operationalInfo <- classDataOperational
    } yield MetaCatalog(ds, operationalInfo, dcatapitInfo)

    res match {
      case Success(schema) => Some(convertToConvSchema(schema).get)
      case Failure(ex) => logger.error(ex.getMessage)
        None
    }

  }
}