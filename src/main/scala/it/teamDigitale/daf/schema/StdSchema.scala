package it.teamDigitale.daf.schema

import play.api.libs.json._

case class StdSchema (
    name: String,
    nameDataset: String,
    uri: String,
    stdSchemaName: String,  //to be deleted
    theme: String,
    cat: Option[Seq[String]],
    groupOwn: String,
    owner: String,
    //src: Map[String, String],
    //fields: JsArray,  //to be deleted
    dataSchema: DataSchema
)