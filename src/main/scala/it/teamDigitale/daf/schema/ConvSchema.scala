package it.teamDigitale.daf.schema

import play.api.libs.json._


case class ConvSchema (
      name: String,
      nameDataset: String,  //to be deleted
      uri: Option[String],
      isStd: Option[Boolean],
      theme: String,
      cat: Option[Seq[String]],
      groupOwn: String,
      owner: String,
      src: Map[String, String],
      dataSchema: DataSchema,
      stdSchemaUri: Option[String],
      reqFields: Option[Seq[Map[String, String]]],  //StdSchema - those are the fields of the input dataset that map to the StdSchema ones.
      custFields: Option[Seq[String]]  //StdSchema - those are the list of field names of the input dataset that are in addition to the StdSchema ones.
  )