package it.teamDigitale.daf.datastructures

import it.gov.daf.catalogmanagerclient.model.DatasetCatalog

case class ConvSchema (
                        name: String,
                        uri: Option[String],
                        isStd: Boolean = false,
                        theme: String,
                        cat: Seq[String] = Seq(),
                        groupOwn: String,
                        owner: String,
                        src: it.gov.daf.catalogmanagerclient.model.InputSrc,
                        dataSchema: DatasetCatalog,
                        stdSchemaUri: Option[String],
                        reqFields: Seq[it.gov.daf.catalogmanagerclient.model.ConversionField] = Seq(),  //StdSchema - those are the fields of the input dataset that map to the StdSchema ones.
                        custFields: Seq[String] = Seq() //StdSchema - those are the list of field names of the input dataset that are in addition to the StdSchema ones.
                      )