package it.teamDigitale.daf.schema

import it.teamDigitale.daf.datastructures.DataSchema.{DatasetSchema, Fields_conv, Input_src}


//import play.api.libs.json._
//case class ConvSchema (
//      name: String,
//      nameDataset: String,  //to be deleted
//      uri: Option[String],
//      isStd: Option[Boolean],
//      theme: String,
//      cat: Option[Seq[String]],
//      groupOwn: String,
//      owner: String,
//      src: Map[String, String],
//      dataSchema: DataSchema,
//      stdSchemaUri: Option[String],
//      reqFields: Option[Seq[Map[String, String]]],  //StdSchema - those are the fields of the input dataset that map to the StdSchema ones.
//      custFields: Option[Seq[String]]  //StdSchema - those are the list of field names of the input dataset that are in addition to the StdSchema ones.
//  )

case class ConvSchema (
                        name: String,
                        uri: Option[String],
                        isStd: Boolean = false,
                        theme: String,
                        cat: Seq[String] = Seq(),
                        groupOwn: String,
                        owner: String,
                        src: Input_src,
                        dataSchema: DatasetSchema,
                        stdSchemaUri: Option[String],
                        reqFields: Seq[Fields_conv] = Seq(),  //StdSchema - those are the fields of the input dataset that map to the StdSchema ones.
                        custFields: Seq[String] = Seq() //StdSchema - those are the list of field names of the input dataset that are in addition to the StdSchema ones.
                      )