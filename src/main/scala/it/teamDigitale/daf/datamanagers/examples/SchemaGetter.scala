package it.teamDigitale.daf.datamanagers.examples

import org.apache.logging.log4j.scala.Logging


//TxtMgmt.normString(s)
// TODO we should rewrite this trait
trait SchemaGetter[C] extends Logging {
  def getSchema(): Option[C]
  
//  def getString(json: JsValue, name: String): Option[String] = {
//    JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)).asOpt[String]
//  }
//
//  def getBoolean(json: JsValue, name: String): Option[Boolean] = {
//    JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)).asOpt[Boolean]
//  }
//  /*
//  def get[T<:AnyRef](json: JsValue, name: String): Option[T] = {
//    JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)).asOpt[T]
//  }
//  *
//  */
//
//  /*
//   * TODO da capire se serve
//  def getBasicInfo(json: JsValue): Map[String, String] = {
//    val isStd = getString(json, "is_std").getOrElse("-1")
//    val groupOwn = getString(json, "group_own").getOrElse("open")
//    val owner = getString(json, "owner").getOrElse("unknown")
//    val theme = getString(json, "theme").getOrElse("unknown")
//    val nameDs = getString(json, "dataset_name").getOrElse("unknown_" + System.currentTimeMillis / 1000 )
//
//
//    val uri = UriDataset(
//        domain = "daf",
//        typeDs: String = "",
//        groupOwn: String = "",
//        owner: String = "",
//        theme: String = "",
//        nameDs: String = "")
//    Map(
//        "isStd" -> ,
//        "groupOwn" -> ,
//        "owner" -> ,
//        "theme" -> ,
//        "nameDs" ->
//    )
//
//  }
//  *
//  */
//
//  //to be deleted
//  def getString2(json: JsValue, name: String, normalize: Boolean = true): Option[String] = {
//    JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)).asOpt[String]
//  }
//
//  def getCat(json: JsValue, name: String): Option[Seq[String]] = {
//    Try((JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)) \\ "val")) match {
//      case Success(x) => {
//        Some(x.map(_.toString).toSeq)
//      }
//      case Failure(err) => {
//        logger.error("Problem in getCat() - " + err.getMessage)
//        None
//      }
//    }
//  }
//
//  def getMap(json: JsValue, name: String): Option[Map[String, String]] = {
//    val fromJson = JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)).validate[Map[String, String]]
//    fromJson match {
//      case s: JsSuccess[Map[String, String]] => Some(s.get)
//      case _ =>{
//        logger.error("Problem in getMap() - name: " + name)
//        None
//      }
//    }
//  }
//
//
//  //TODO do src as a case class!!! Think about it
//  def getSrc(json: JsValue, name: String): Option[Map[String, String]] = {
//    val fromJson = (json \ name).validate[Map[String, String]]
//    fromJson match {
//      case s: JsSuccess[Map[String, String]] => Some(s.get)
//      case _ =>{
//        println("getConvSchema - getSrc - errore: " + name)
//        None
//      }
//    }
//  }
//
//  def getFields(json: JsValue, name: String): Option[Seq[String]]  = {
//    val fromJson = JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)).asOpt[Seq[Map[String, String]]]
//
//    fromJson match {
//
//      case Some(s) if (s.map{
//          x => x.contains("name")
//          //x.as[JsObject].keys.contains("formula")
//        }.forall(x => x==true)) => Some(s.map(x=>x("name")))
//
//      //None in all other cases
//      case _ => {
//        //logger.error("Error in getFields(), name: " + name)
//        None
//      }
//    }
//  }
//
//  def getGroupOwn(json: JsValue, name: String): String = {
//    val fromJson = (json \ name).asOpt[String]
//    fromJson match {
//      case Some(s) => s
//      case _ =>{
//        println("Error: getGroupOwn:" +  name + " Ownership set to 'open'.")
//        "open"
//      }
//    }
//  }
//
//  def getDataSchema(json: JsValue, name: String): DataSchema2 = {
//    //define an implicit Reads to implement DataSchema and DataSchemaField conversion form JSON
//
//    implicit val DataSchemaFieldReads: Reads[DataSchemaField] = (
//      (JsPath \ "name").read[String] and
//      (JsPath \ "type").read[JsValue] and
//      ((JsPath \ "doc").read[String] or Reads.pure("")) and
//      ((JsPath \ "default").read[String] or Reads.pure("")) and
//      (JsPath \ "metadata").read[JsValue]
//    )(DataSchemaField.apply _)
//
//    implicit val DataSchemaReads: Reads[DataSchema2] = (
//      (JsPath \ "namespace").read[String] and
//      (JsPath \ "type").read[String] and
//      (JsPath \ "name").read[String] and
//      ((JsPath \ "aliases").read[Seq[String]] or Reads.pure(Seq(""))) and
//      (JsPath \ "fields").read[Seq[DataSchemaField]]
//    )(DataSchema2.apply _)
//
//    Try(JsonMgmt.jsonRec(json, MetadataLU.getFieldSeq(name)).as[DataSchema2]) match {
//      case Success(x) => x
//      case Failure(err) => {
//        logger.error("Fatal Error: Problem in getDataSchema() - " + err.getMessage)
//        sys.exit(1)
//      }
//    }
//  }
    
  
}