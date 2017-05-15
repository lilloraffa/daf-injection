package it.teamDigitale.daf.utils

import play.api.libs.json._
import java.io.FileInputStream

object JsonMgmt {
  
  def getJson(filePath: String): JsValue = {
    val stream = new FileInputStream(filePath)
    val json = try {  Json.parse(stream) } finally { stream.close() }
    json
  }


  def jsonRec(json: JsValue, nestFields: Seq[String]) = {
    def jsonRecFunc(obj: JsValue, accumulator: Integer): JsValue = {
      if (accumulator == (nestFields.length - 1)) {
        obj
      } else {
        jsonRecFunc((obj \ nestFields(accumulator)).asOpt[JsValue].getOrElse(Json.obj()), (accumulator + 1))
      }
      
    }
    (jsonRecFunc(json, 0) \ nestFields.last)
  }
}
