package it.teamDigitale.daf.datamanagers.examples

import it.gov.daf.catalogmanagerclient.model.{DatasetCatalog, DcatApIt, MetaCatalog, Operational}
import it.teamDigitale.daf.datastructures.Model._
import it.teamDigitale.daf.datastructures.StdSchema
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging
import it.teamDigitale.daf.datastructures._
import scala.util.{Failure, Success, Try}


/**
  * This class is just for test.
  * Generate the StdSchema extracted from json files in /resources/stdSchema
  *
  * @param uri contains the uri that should return all informations related to the StdSchema.
  */
class StdSchemaGetter(uri: String) extends SchemaGetter[StdSchema] with Logging {
  def getSchema() = {
    val stream_operational = getClass.getResourceAsStream("/stdSchema/std-operational.json")
    val stream_dcatap = getClass.getResourceAsStream("/stdSchema/std-dcatapit.json")
    val stream_dataschema = getClass.getResourceAsStream("/stdSchema/std-dataschema.json")

    val classDataSchema = Try(JsonConverter.fromJson[DatasetCatalog](stream_dataschema))
    val classDataDcatapit = Try(JsonConverter.fromJson[DcatApIt](stream_dcatap))
    val classDataOperational = Try(JsonConverter.fromJson[Operational](stream_operational))

    val res: Try[MetaCatalog] = for {
      ds <- classDataSchema
      dcatapitInfo <- classDataDcatapit
      operationalInfo <- classDataOperational
    } yield MetaCatalog(ds,  operationalInfo, dcatapitInfo)

    res match {
      case Success(schema) =>
        Some(convertToStdSchema(schema).get)
      case Failure(ex) => logger.error(ex.getMessage)
        None
    }

  }
  
  


}