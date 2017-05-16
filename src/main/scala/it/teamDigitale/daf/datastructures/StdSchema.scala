package it.teamDigitale.daf.datastructures

import it.teamDigitale.daf.datastructures.Model.DatasetSchema

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