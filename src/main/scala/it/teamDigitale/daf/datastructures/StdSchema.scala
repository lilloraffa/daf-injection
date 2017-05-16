package it.teamDigitale.daf.schema

import it.teamDigitale.daf.datastructures.DataSchema.DatasetSchema
import it.teamDigitale.daf.datastructures.StandardSchema.StdDatasetSchema

//case class StdSchema (
//    name: String,
//    nameDataset: String,
//    uri: String,
//    stdSchemaName: String,  //to be deleted
//    theme: String,
//    cat: Option[Seq[String]],
//    groupOwn: String,
//    owner: String,
//    //src: Map[String, String],
//    //fields: JsArray,  //to be deleted
//    dataSchema: DataSchema2
//)

case class StdSchema (
                       name: String = "",
                       nameDataset: String,
                       uri: String,
                       stdSchemaName: String = "",  //to be deleted
                       theme: String,
                       cat: Seq[String] = Seq(),
                       groupOwn: String,
                       owner: String,
                       dataSchema: DatasetSchema
                     )