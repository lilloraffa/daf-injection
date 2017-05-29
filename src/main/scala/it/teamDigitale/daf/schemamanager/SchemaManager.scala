package it.teamDigitale.daf.schemamanager

import it.gov.daf.catalogmanagerclient.model.MetaCatalog
import it.teamDigitale.daf.datastructures.Model.{DatasetType, Schema}
import it.teamDigitale.daf.datastructures.uri.UriDataset
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging

import scala.util.Try

/**
  * Created by fabiana on 17/05/17.
  *
  * This class manages the schema associated to datasets.
  * Schema info are sent either via a web-form or by directly calling the catalogds/add/{info} API
  * (see the related doc for more info on the API).
  */
class SchemaManager extends Logging {

  /**
    * @param uriDataset to query
    * @return schema associated to the input uri
    */
  def getSchemaFromUri(uriCatalog: String, uriDataset: String): Try[MetaCatalog] = {
    lazy val request = s"$uriCatalog/$uriDataset"
    //TODO send http request from the web server
    val httpRequest = getDatafromHttp(request)

    for {
      data <- httpRequest
      schema <- getSchemaFromJson(data)
    } yield schema

  }

  def getDatafromHttp(url: String) = Try(scala.io.Source.fromURL(url).mkString)

  /**
    * Given a json the method convert the data into a schema enriching its information with the uri
    * @param json
    * @return a schema parsing the input json
    */
  def getSchemaFromJson(json: String): Try[MetaCatalog] = {

    val tryschema = Try(JsonConverter.fromJson[MetaCatalog](json))

//    val schema = tryschema.flatMap { s =>
//      s.operational.uri match {
//        case Some(_) => Try(s)
//        case None =>
//          for {
//            newUri <- it.teamDigitale.daf.datastructures.convertToUriDatabase(s).map(_.getUri())
//            operational <- Try(s.operational.copy(uri = newUri))
//            newSchema <- Try(s.copy( operational = operational))
//          } yield newSchema
//
//      }
//    }

    val schema = tryschema.flatMap { s =>
          for {
            newUri <- it.teamDigitale.daf.datastructures.convertToUriDatabase(s).map(_.getUri())
            operational <- Try(s.operational.copy(uri = newUri))
            newSchema <- Try(s.copy( operational = operational))
          } yield newSchema
    }

    schema
  }


}
