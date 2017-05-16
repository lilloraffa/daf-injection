package it.teamdigitale.daf.datastructures
import java.io.InputStream

import it.teamDigitale.daf.datastructures.Model._
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging
import org.scalatest.FunSuite

import scala.util.Try

/**
  * Created by fabiana on 15/05/17.
  */
class JsonConverterTest extends FunSuite with Logging{

  test("The jsons on dataschema folder should be correctly converted in case classes"){

    val stream_operational: InputStream = getClass.getResourceAsStream("/dataschema/data-operational.json")
    val stream_dcatap = getClass.getResourceAsStream("/dataschema/data-dcatapit.json")
    val stream_dataschema = getClass.getResourceAsStream("/dataschema/data-dataschema.json")

    val classDataSchema = Try(JsonConverter.fromJson[DatasetSchema](stream_dataschema))
    val classDataDcatapit = Try(JsonConverter.fromJson[DcatapitInfo](stream_dcatap))
    val classDataOperational = Try(JsonConverter.fromJson[OperationalInfo](stream_operational))

    assert(classDataSchema.isSuccess)
    assert(classDataDcatapit.isSuccess)
    assert(classDataOperational.isSuccess)
    assert(classDataOperational.get.uri.isEmpty)

    logger.info(classDataSchema)
    logger.info(classDataDcatapit)
    logger.info(classDataOperational)

    val schema = Schema(classDataSchema.get, classDataDcatapit.get, classDataOperational.get)
    val convSchema = schema.convertToConvSchema()
    assert(convSchema.uri.isEmpty)
    assert(convSchema.name == "GTFS Agency" )
    assert(!convSchema.isStd)
    assert(convSchema.theme == "Trasporti")
    assert(convSchema.cat.head == "public transport")
    assert(convSchema.groupOwn == "open")
    assert(convSchema.owner == "Torino")
    assert(convSchema.dataSchema.namespace == "it.gov.daf.dataset.open.torino")
    assert(convSchema.dataSchema.`type` == "record")
    assert(convSchema.dataSchema.name =="GTFS_Agency")
    assert(convSchema.dataSchema.aliases.head =="-1")
    assert(convSchema.dataSchema.fields.size == 6)
    assert(convSchema.dataSchema.fields.head.name == "agency_id")
    assert(convSchema.dataSchema.fields.head.doc == "")
    assert(convSchema.dataSchema.fields.head.metadata.tag.size ==3)
    assert(convSchema.dataSchema.fields.head.metadata.semantics.`@id` == "http://vocab.gtfs.org/terms#Agency_id")
    assert(convSchema.dataSchema.fields.head.metadata.cat == "mobility/transportation/agency/name")


    val file: String = getClass.getResource("/agency.txt").getPath
    //TODO we should fix this
    assert(convSchema.src.name == null)
    assert(convSchema.src.ing_type == "pull")
    assert(convSchema.src.src_type == "csv")


  }

  test("The jsons on stdSchema folder should be correctly converted in case classes"){

    val stream_operational: InputStream = getClass.getResourceAsStream("/stdSchema/std-operational.json")
    val stream_dcatap = getClass.getResourceAsStream("/stdSchema/std-dcatapit.json")
    val stream_dataschema = getClass.getResourceAsStream("/stdSchema/std-dataschema.json")

    val classDataSchema = Try(JsonConverter.fromJson[DatasetSchema](stream_dataschema))
    val classDataDcatapit = Try(JsonConverter.fromJson[DcatapitInfo](stream_dcatap))
    val classDataOperational = Try(JsonConverter.fromJson[OperationalInfo](stream_operational))

    assert(classDataSchema.isSuccess)
    assert(classDataDcatapit.isSuccess)
    assert(classDataOperational.isSuccess)

    logger.info(classDataSchema)
    logger.info(classDataDcatapit)
    logger.info(classDataOperational)
  }

}
