package it.teamDigitale.daf.datamanagers

import java.lang.Throwable

import com.typesafe.config.{Config, ConfigFactory}
import it.teamDigitale.daf.datastructures.Model.{DcatapitInfo, Schema}
import it.teamDigitale.daf.utils.JsonConverter
import org.apache.logging.log4j.scala.Logging

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.util.control.Exception

/**
  * Created by fabiana on 17/05/17.
  *
  * Metadata info are sent either via a webform or by directly calling the catalogds/add/{info} API
  * (see the related doc for more info on the API).
  */
class DataManager(uriCatalog: String) extends Logging{

  //val uriCatalog = ConfigFactory.load().getString("")


  /**
    * @param uriDataset to query
    * @return schema associated to the input uri
    */
  def getSchemaFromUri(uriDataset: String): Try[Schema] = {
    lazy val request = s"$uriCatalog/$uriDataset" // s"http://$ipCatalog/catalog-manager/v1/dataset-catalogs/$uri"
    //TODO send http request from the web server
    val httpRequest = getDatafromHttp(request)

    for {
      data <- httpRequest
      schema <- getSchemaFromJson(data)
    } yield schema

  }

  def getDatafromHttp(url: String) = Try(scala.io.Source.fromURL(url).mkString)

  /**
    *
    * @param json
    * @return a schema parsing the input json
    */
  def getSchemaFromJson(json: String): Try[Schema] = Try(JsonConverter.fromJson[Schema](json))
}
