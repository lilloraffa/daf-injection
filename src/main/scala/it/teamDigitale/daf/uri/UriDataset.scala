package it.teamDigitale.daf.uri

import scala.util.{Try, Success, Failure}
import org.apache.logging.log4j.scala.Logging
import org.apache.logging.log4j.Level
import com.typesafe.config.ConfigFactory

case class UriDataset(
      domain: String = "",
      typeDs: String = "",
      groupOwn: String = "",
      owner: String = "",
      theme: String = "",
      nameDs: String = "") {
  
  val config = ConfigFactory.load()
  
  def gerUri(): String = {
    domain + "://" + typeDs + "/" + groupOwn + "/" + owner + "/" + theme + "/" + nameDs
  }

  
  def getUrl(): String = {
    
    val basePath = config.getString("Inj-properties.hdfsBasePath")
	  val baseDataPath = config.getString("Inj-properties.dataBasePath")
    typeDs match {
      case "std" => basePath + baseDataPath + "/" + typeDs + "/" + theme + "/" + groupOwn + "/" + nameDs
      case "ord" => basePath + baseDataPath + "/" + typeDs + "/" + owner + "/" + theme + "/" + groupOwn + "/" + nameDs
      case _ => "-1"
    }
  }
}
      
object UriDataset extends Logging{
  def apply(uri: String): UriDataset = {
    Try {
      val uri2split = uri.split("://")
      val uriParts = uri2split(1).split("/")
      new UriDataset(
          domain = uri2split(0),
          typeDs = uriParts(1),
          groupOwn = uriParts(2),
          owner = uriParts(3),
          theme = uriParts(4),
          nameDs = uriParts(5))
    } match {
      case Success(s) => s
      case Failure(err) => {
        logger.error("Error while creating uri: " + uri + " - " + err.getMessage)
        UriDataset()
      }
    }
    
  }
}