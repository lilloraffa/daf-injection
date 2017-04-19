package it.teamDigitale.daf.schema

import play.api.libs.json._


case class ConvSchema (
      name: String,
      nameDataset: String,
      cat: Option[Seq[String]],
      groupOwn: String,
      owner: String,
      src: Map[String, String],
      stdSchemaName: Option[String],
      reqFields: List[Map[String, String]],
      custFields: Option[JsArray]  //Put as JsArray for now
  )