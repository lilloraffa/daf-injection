package it.teamDigitale.daf.schema

import play.api.libs.json._

case class DataSchemaField (
    name: String,
    typeField: JsValue,
    doc: String = "",
    default: String = "",
    metadata: JsValue = Json.parse("{}")
)