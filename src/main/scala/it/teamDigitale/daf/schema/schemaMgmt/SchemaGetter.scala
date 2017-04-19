package it.teamDigitale.daf.schema.schemaMgmt

import play.api.libs.json._
import it.teamDigitale.daf.utils.TxtMgmt
import play.api.libs.json.JsValue.jsValueToJsLookup

trait SchemaGetter[C] {
  def getSchema(): Option[C]
  /*
  def getString(json: JsValue, name: String, normalize: Boolean = false): String = {
    val fromJson = (json \ name).asOpt[String]
    fromJson match {
      case Some(s) => if(normalize) (TxtMgmt.normString(s)) else s
      case _ =>{
        println("SchemaGetter - getString - errore: " + name)
        ""
      }
    }
  }
  * 
  */
  
  def getString(json: JsValue, name: String, normalize: Boolean = false): Option[String] = {
    val fromJson = (json \ name).asOpt[String]
    fromJson match {
      case Some(s) => if(normalize) Some(TxtMgmt.normString(s)) else Some(s)
      case _ =>{
        println("getConvSchema - Errore in getStringOpt: " + name)
        None
      }
    }
  }
  /*
  def getCat(json: JsValue, name: String): Seq[String] = {
    val fromJson = (json \ name).asOpt[Seq[String]]
    fromJson match {
      case Some(s) => s
      case _ =>{
        println("getConvSchema - getCat - No categories defined: " + name)
        Seq()
      }
    }
  }
  * 
  */
  
  def getCat(json: JsValue, name: String): Option[Seq[String]] = {
    val fromJson = (json \ name).asOpt[Seq[String]]
    fromJson match {
      case Some(s) => Some(s)
      case _ =>{
        println("getConvSchema - getCat - No categories defined: " + name)
        None
      }
    }
  }
  
  //TODO do src as a case class!!! Think about it
  def getSrc(json: JsValue, name: String): Option[Map[String, String]] = {
    val fromJson = (json \ name).validate[Map[String, String]]
    fromJson match {
      case s: JsSuccess[Map[String, String]] => Some(s.get)
      case _ =>{
        println("getConvSchema - getSrc - errore: " + name)
        None
      }
    }
  }
  
  def getFields(json: JsValue, name: String): Option[JsArray]  = {
    val fromJson = (json \ name).asOpt[JsArray]
    
    val listField: Option[JsArray] = fromJson match {
      
      case Some(s) if (s.value.map{
          x => x.as[JsObject].keys.contains("name") & 
          true  
          //x.as[JsObject].keys.contains("formula")
        }.forall(x => x==true)) => Some(s)
      
      //None in all other cases
      case _ => {println("ConvSchema - No Custom Field Present")
        None
      }
    }
    
    listField
  }
  
  def getGroupOwn(json: JsValue, name: String): String = {
      val fromJson = (json \ name).asOpt[String]
      fromJson match {
        case Some(s) => s
        case _ =>{
          println("Error: getGroupOwn:" +  name + " Ownership set to 'open'.")
          "open"
        }
      }
    }
  
}