package it.teamdigitale.daf.datastructures
import java.io.InputStream

import it.gov.daf.catalogmanagerclient.model.{DatasetCatalog, DcatApIt, MetaCatalog, Operational}
import it.teamDigitale.daf.datastructures.Model._
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging
import org.scalatest.FunSuite

import scala.util.Try
import it.teamDigitale.daf.datastructures._

/**
  * Created by fabiana on 15/05/17.
  */
class JsonConverterTest extends FunSuite with Logging{

  test("The jsons on dataschema folder should be correctly converted in case classes"){

    val stream_operational: InputStream = getClass.getResourceAsStream("/dataschema/data-operational.json")
    val stream_dcatap = getClass.getResourceAsStream("/dataschema/data-dcatapit.json")
    val stream_dataschema = getClass.getResourceAsStream("/dataschema/data-dataschema.json")

    val classDataSchema = Try(JsonConverter.fromJson[DatasetCatalog](stream_dataschema))
    val classDataDcatapit = Try(JsonConverter.fromJson[DcatApIt](stream_dcatap))
    val classDataOperational = Try(JsonConverter.fromJson[Operational](stream_operational))

    assert(classDataSchema.isSuccess)
    assert(classDataDcatapit.isSuccess)
    assert(classDataOperational.isSuccess)
    assert(classDataOperational.get.logicalUri.isEmpty)

    logger.info(classDataSchema)
    logger.info(classDataDcatapit)
    logger.info(classDataOperational)

    val schema = MetaCatalog(classDataSchema.get, classDataOperational.get ,classDataDcatapit.get)
    val convSchema = convertToConvSchema(schema).get
    assert(convSchema.logicalUri.isEmpty)
    assert(convSchema.name == "GTFS Agency" )
    assert(!convSchema.isStd)
    assert(convSchema.theme == "Trasporti")
    assert(convSchema.cat.head == "public transport")
    assert(convSchema.groupOwn == "open")
    assert(convSchema.owner == "Torino")
    assert(convSchema.dataSchema.namespace == "it.gov.daf.dataset.open.torino")
    assert(convSchema.dataSchema._type == "record")
    assert(convSchema.dataSchema.name =="GTFS_Agency")
    //assert(convSchema.dataSchema.aliases.head =="-1")
    assert(convSchema.dataSchema.fields.size == 6)
    assert(convSchema.dataSchema.fields.head.name == "agency_id")
    assert(convSchema.dataSchema.fields.head.doc == "")
    assert(convSchema.dataSchema.fields.head.metadata.tag.size ==3)
    assert(convSchema.dataSchema.fields.head.metadata.semantics.id == "http://vocab.gtfs.org/terms#Agency_id")
    assert(convSchema.dataSchema.fields.head.metadata.cat == "mobility/transportation/agency/name")


    val file: String = getClass.getResource("/agency.txt").getPath
    //TODO we should fix this
    //assert(convSchema.src.name == null)
    assert(convSchema.src.ingType == "pull")
    assert(convSchema.src.srcType == "csv")


  }

  test("check Operational info string is correctly converted") {
    val json =
      """
        |{
        |  "uri": "daf://dataset/std/open/daf/trasposti/gtfs_agency",
        |  "is_std": 1,
        |  "group_own": "pippo",
        |  "read_type": "update",
        |  "location":
        |  [
        |    {
        |      "lat": 45.07049,
        |      "lon": 7.68682
        |    }
        |  ]
        |  ,
        |  "input_src":
        |    {
        |      "ing_type": "pull",
        |      "src_type": "csv",
        |      "url": "156.54.180.185/ftp/torino/gtfs_agency.txt"
        |    }
        |
        |}
        |
      """.mkString.replace("\n", "").replace("|","").trim

    val operational = JsonConverter.fromJson[Operational](json)
    assert(operational.inputSrc.url != null)
    assert(operational.groupOwn == "pippo")
  }

  test("The jsons on stdSchema folder should be correctly converted in case classes"){

    val stream_operational: InputStream = getClass.getResourceAsStream("/stdSchema/std-operational.json")
    val stream_dcatap = getClass.getResourceAsStream("/stdSchema/std-dcatapit.json")
    val stream_dataschema = getClass.getResourceAsStream("/stdSchema/std-dataschema.json")

    val classDataSchema = Try(JsonConverter.fromJson[DatasetCatalog](stream_dataschema))
    val classDataDcatapit = Try(JsonConverter.fromJson[DcatApIt](stream_dcatap))
    val classDataOperational = Try(JsonConverter.fromJson[Operational](stream_operational))

    assert(classDataSchema.isSuccess)
    assert(classDataDcatapit.isSuccess)
    assert(classDataOperational.isSuccess)

    logger.info(classDataSchema)
    logger.info(classDataDcatapit)
    logger.info(classDataOperational)
  }

}
