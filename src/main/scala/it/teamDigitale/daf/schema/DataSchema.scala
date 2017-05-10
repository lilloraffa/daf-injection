package it.teamDigitale.daf.schema

case class DataSchema (
    namespace: String = "",
    typeField: String = "record",
    name: String = "",
    aliases: Seq[String] = Seq(),
    fields: Seq[DataSchemaField]
)