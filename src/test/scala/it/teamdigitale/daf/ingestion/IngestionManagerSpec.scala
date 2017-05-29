package it.teamdigitale.daf.ingestion

import it.gov.daf.catalogmanagerclient.model.{DatasetCatalog, DcatApIt, MetaCatalog, Operational}
import it.teamDigitale.daf.datamanagers.examples.ConvSchemaGetter.getClass
import it.teamDigitale.daf.datastructures.ConvSchema
import it.teamDigitale.daf.ingestion.IngestionManager
import it.teamDigitale.daf.schemamanager.SchemaManager
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging
import org.mockito.stubbing.OngoingStubbing
import org.specs2.mutable.Specification

import scala.util.{Success, Try}

/**
  * Created by fabiana on 23/05/17.
  */
class IngestionManagerSpec extends Specification with Logging {


  def getTestSchema() : Try[MetaCatalog] = {

      val stream_operational = getClass.getResourceAsStream("/dataschema/data-operational.json")
      val stream_dcatap = getClass.getResourceAsStream("/dataschema/data-dcatapit.json")
      val stream_dataschema = getClass.getResourceAsStream("/dataschema/data-dataschema.json")

      val classDataSchema = Try(JsonConverter.fromJson[DatasetCatalog](stream_dataschema))
      val classDataDcatapit = Try(JsonConverter.fromJson[DcatApIt](stream_dcatap))
      val classDataOperational = Try(JsonConverter.fromJson[Operational](stream_operational))

      for {
        ds <- classDataSchema
        dcatapitInfo <- classDataDcatapit
        operationalInfo <- classDataOperational
      } yield MetaCatalog(ds, operationalInfo, dcatapitInfo)
  }

  "The Injestion Manager" should {
    "correctly store data if all information are provided" in {
      val injManager = new IngestionManager
      val fileSchema = getClass.getResource("/stdSchema/std-dataschema.json").getPath

      println(fileSchema)
      val dataset = getClass.getResource("/agency.txt").getPath
      val avroSchema = Try(injManager.readAvroSchema(fileSchema))


      val schema = getTestSchema().get

      val check = injManager.write(schema)
      //println(avroSchema.get)
     // avroSchema.isSuccess must be equals true
     // "Hello world" must haveSize(11)
      avroSchema must be beSuccessfulTry

      check must equalTo(true)
    }

  }
}