package it.teamDigitale.daf.utils

import play.api.libs.json._
import java.io.FileInputStream

object JsonMgmt {
  
  def getJson(filePath: String): JsValue = {
    val stream = new FileInputStream(filePath)
    val json = try {  Json.parse(stream) } finally { stream.close() }
    json
  }
}