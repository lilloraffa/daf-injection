package it.teamDigitale.daf.schema

import play.api.libs.json._

case class StdSchema (
    name: String,
    stdSchemaName: String,
    cat: Seq[String],
    groupOwn: String,
    owner: String,
    src: Map[String, String],
    fields: JsArray
)