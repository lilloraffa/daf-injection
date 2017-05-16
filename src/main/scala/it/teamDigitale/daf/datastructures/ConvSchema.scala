package it.teamDigitale.daf.datastructures

import it.teamDigitale.daf.datastructures.Model.{DatasetSchema, Fields_conv, Input_src}

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